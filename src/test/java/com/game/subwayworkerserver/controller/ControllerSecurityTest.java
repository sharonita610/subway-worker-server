package com.game.subwayworkerserver.controller;

import com.game.subwayworkerserver.config.JsonAuthenticationEntryPoint;
import com.game.subwayworkerserver.config.SecurityConfig;
import com.game.subwayworkerserver.service.coworker.CoworkerService;
import com.game.subwayworkerserver.service.stage.StageService;
import com.game.subwayworkerserver.service.store.StoreService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** X-User-ID 헤더 기반 인증이 컨트롤러까지 어떻게 이어지는지 고정한다. */
@WebMvcTest({StoreController.class, StageController.class})
@Import({SecurityConfig.class, JsonAuthenticationEntryPoint.class})
class ControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private StoreService storeService;
    @MockBean private StageService stageService;
    @MockBean private CoworkerService coworkerService;

    @Test
    void 보호된_경로에_헤더가_없으면_401과_JSON_에러를_돌려준다() throws Exception {
        mockMvc.perform(get("/store"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication required."));

        verify(storeService, never()).getStoreItems(anyString());
    }

    @Test
    void 헤더가_있으면_uuid가_CurrentUuid_매개변수로_서비스까지_전달된다() throws Exception {
        mockMvc.perform(get("/store").header("X-User-ID", "user-uuid-1"))
                .andExpect(status().isOk());

        verify(storeService).getStoreItems("user-uuid-1");
    }

    @Test
    void 공개_경로는_헤더_없이도_접근된다() throws Exception {
        mockMvc.perform(get("/stage/monsters").param("num", "1"))
                .andExpect(status().isOk());

        verify(stageService).getStageMonsters(1);
    }
}
