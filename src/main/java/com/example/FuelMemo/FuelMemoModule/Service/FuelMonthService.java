package com.example.FuelMemo.FuelMemoModule.Service;

import com.example.FuelMemo.Shared.Response.MessageResponse;

public interface FuelMonthService {

    MessageResponse closeFuelMonth(Integer month, Integer year);

    MessageResponse reopenFuelMonth(Integer month, Integer year);
}
