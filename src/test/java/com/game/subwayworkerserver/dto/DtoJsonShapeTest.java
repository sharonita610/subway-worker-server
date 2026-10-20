package com.game.subwayworkerserver.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * dto 패키지의 모든 record(중첩 포함)를 샘플 값으로 만들어 직렬화한 JSON 키 구조를 스냅샷과 비교한다.
 * 응답 형식이 바뀌면 여기서 먼저 실패한다. 의도한 변경이면 build/dto-json-shapes.txt를 스냅샷 파일로 복사한다.
 */
class DtoJsonShapeTest {

    private static final Path SNAPSHOT = Path.of("src/test/resources/dto-json-shapes.txt");
    private static final Path GENERATED = Path.of("build/dto-json-shapes.txt");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    void 모든_DTO의_JSON_키_구조가_스냅샷과_같다() throws Exception {
        Map<String, String> shapes = new TreeMap<>();
        for (Class<?> record : dtoRecords()) {
            Object sample = sample(record, 0);
            JsonNode node = MAPPER.valueToTree(sample);
            List<String> paths = new ArrayList<>();
            collectPaths(node, "", paths);
            shapes.put(record.getName().replace("com.game.subwayworkerserver.dto.", ""), String.join(", ", paths));
        }
        StringBuilder rendered = new StringBuilder();
        shapes.forEach((name, shape) -> rendered.append(name).append(": ").append(shape).append('\n'));

        Files.createDirectories(GENERATED.getParent());
        Files.writeString(GENERATED, rendered.toString());

        if (!Files.exists(SNAPSHOT)) {
            fail("스냅샷이 없습니다. " + GENERATED + " 를 " + SNAPSHOT + " 로 복사하세요.");
        }
        assertThat(rendered.toString())
                .as("DTO JSON 구조가 스냅샷과 다릅니다. 의도한 변경이면 %s 를 %s 로 복사하세요.", GENERATED, SNAPSHOT)
                .isEqualTo(Files.readString(SNAPSHOT));
        assertThat(shapes).hasSizeGreaterThan(60);
    }

    private static List<Class<?>> dtoRecords() {
        return new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.game.subwayworkerserver.dto")
                .stream()
                .map(JavaClass::reflect)
                .filter(Class::isRecord)
                .sorted((a, b) -> a.getName().compareTo(b.getName()))
                .toList();
    }

    private static void collectPaths(JsonNode node, String prefix, List<String> out) {
        if (node.isObject()) {
            for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> e = it.next();
                String path = prefix.isEmpty() ? e.getKey() : prefix + "." + e.getKey();
                if (e.getValue().isObject() || e.getValue().isArray()) {
                    collectPaths(e.getValue(), path, out);
                } else {
                    out.add(path);
                }
            }
        } else if (node.isArray()) {
            if (node.isEmpty()) {
                out.add(prefix + "[]");
            } else {
                collectPaths(node.get(0), prefix + "[]", out);
            }
        } else {
            out.add(prefix);
        }
    }

    private static Object sample(Type type, int depth) throws Exception {
        if (type instanceof ParameterizedType p) {
            Class<?> raw = (Class<?>) p.getRawType();
            if (List.class.isAssignableFrom(raw)) {
                return List.of(sample(p.getActualTypeArguments()[0], depth));
            }
            if (Map.class.isAssignableFrom(raw)) {
                return Map.of();
            }
            return null;
        }
        if (!(type instanceof Class<?> c)) {
            return null;
        }
        if (c == String.class) return "text";
        if (c == int.class || c == Integer.class) return 1;
        if (c == long.class || c == Long.class) return 1L;
        if (c == double.class || c == Double.class) return 1.5;
        if (c == boolean.class || c == Boolean.class) return true;
        if (c == LocalDateTime.class) return LocalDateTime.of(2026, 1, 1, 0, 0);
        if (c == LocalDate.class) return LocalDate.of(2026, 1, 1);
        if (c.isEnum()) return c.getEnumConstants()[0];
        if (c.isRecord() && depth < 6) {
            RecordComponent[] components = c.getRecordComponents();
            Class<?>[] types = new Class<?>[components.length];
            Object[] args = new Object[components.length];
            for (int i = 0; i < components.length; i++) {
                types[i] = components[i].getType();
                args[i] = sample(components[i].getGenericType(), depth + 1);
            }
            Constructor<?> ctor = c.getDeclaredConstructor(types);
            ctor.setAccessible(true);
            return ctor.newInstance(args);
        }
        return null;
    }
}
