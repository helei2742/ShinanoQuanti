package com.helei.tradedatacenter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssetInfo {

    private String asset;

    private double free;

    private double locked;
}
