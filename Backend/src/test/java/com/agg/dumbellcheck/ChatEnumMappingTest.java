package com.agg.dumbellcheck;

import com.agg.dumbellcheck.entities.ChatEntity;
import com.agg.dumbellcheck.entities.TipoChat;
import com.agg.dumbellcheck.repositories.ChatRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChatEnumMappingTest {

    @Autowired
    private ChatRepository chatRepository;

    @Test
    void findChatsForUser_mapsDbEnumValues() {
        List<ChatEntity> chats = chatRepository.findChatsForUser(1);
        assertThat(chats).isNotEmpty();
        assertThat(chats.get(0).getTipo()).isEqualTo(TipoChat.grupo);
    }
}
