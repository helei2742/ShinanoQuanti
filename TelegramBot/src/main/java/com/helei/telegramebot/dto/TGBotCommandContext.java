package com.helei.telegramebot.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TGBotCommandContext {

    private String namespace;

    private String command;

    private List<String> params;

}
