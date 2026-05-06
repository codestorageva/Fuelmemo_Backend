package com.example.FuelMemo.Shared.Response;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MapResponse {
    Map<String, Object> response = new HashMap<>();
}
