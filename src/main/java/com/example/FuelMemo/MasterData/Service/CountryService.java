package com.example.FuelMemo.MasterData.Service;

import com.example.FuelMemo.MasterData.Dto.CountryDto;
import com.example.FuelMemo.MasterData.Dto.UpdateDto.CountryUpdateDto;
import com.example.FuelMemo.Shared.Response.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface CountryService {
    MessageResponse addCountry(CountryDto countryDto, Map<String, String> headers);

    DataResponse getCountryById(int id, Map<String, String> headers);

    HttpResponse getAllCountries(String keyword, int pageNumber, int pageSize, String sortBy, String sortDirection, String isDeleted, String status, Map<String, String> headers);

    MessageResponse updateCountry(CountryUpdateDto country, int id, Map<String, String> headers);

//    MessageResponse deleteCountryById(int id,Map<String, String> headers);

    MessageResponse softDeleteCountryById(int id, Map<String, String> headers);

    MessageResponse restoreCountryById(int id, Map<String, String> headers);

    MessageResponse uploadCountryCSV(MultipartFile file, Map<String, String> headers);

    ListResponse getAllCountriesByStatusAndIsDeleted(Map<String, String> headers);

    ListResponse exportedCountryData(Map<String, String> headers);

    LongResponse getTotalCountry();
}
