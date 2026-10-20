package com.game.subwayworkerserver;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * README의 패키지 의존 규칙을 고정한다. 규칙이 깨지면 이 테스트가 먼저 실패한다.
 */
@AnalyzeClasses(packages = "com.game.subwayworkerserver", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule 웹_계층은_컨트롤러만_안다 = noClasses()
            .that().resideOutsideOfPackage("..controller..")
            .should().dependOnClassesThat().resideInAPackage("..controller..")
            .because("service, domain, entity, dto는 controller를 몰라야 한다");

    @ArchTest
    static final ArchRule 컨트롤러는_영속성을_직접_만지지_않는다 = noClasses()
            .that().resideInAPackage("..controller..")
            .should().dependOnClassesThat().resideInAnyPackage("..repository..", "..entity..")
            .because("컨트롤러는 service와 dto만 사용한다");

    @ArchTest
    static final ArchRule 도메인_엔티티_리포지토리는_서비스와_DTO를_모른다 = noClasses()
            .that().resideInAnyPackage("..domain..", "..entity..", "..repository..")
            .should().dependOnClassesThat().resideInAnyPackage("..service..", "..dto..")
            .because("하위 계층은 상위 계층에 의존하지 않는다");

    @ArchTest
    static final ArchRule DTO는_서비스와_리포지토리를_모른다 = noClasses()
            .that().resideInAPackage("..dto..")
            .should().dependOnClassesThat().resideInAnyPackage("..service..", "..repository..")
            .because("dto는 domain과 entity만 변환한다");

    @ArchTest
    static final ArchRule service_common은_다른_도메인에_의존하지_않는다 = noClasses()
            .that().resideInAPackage("..service.common..")
            .should().dependOnClassesThat(resideInAPackage("..service..").and(resideOutsideOfPackage("..service.common..")))
            .because("common은 모든 도메인이 공유하므로 도메인 쪽으로 의존이 흐르면 순환이 생긴다");

    @ArchTest
    static final ArchRule 서비스_도메인_패키지_사이에_순환이_없다 = slices()
            .matching("com.game.subwayworkerserver.service.(*)..")
            .should().beFreeOfCycles();
}
