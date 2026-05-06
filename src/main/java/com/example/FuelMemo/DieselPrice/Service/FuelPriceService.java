package com.example.FuelMemo.DieselPrice.Service;

import com.example.FuelMemo.DieselPrice.Dto.FuelApiResponse;
import com.example.FuelMemo.DieselPrice.Entity.DieselPrice;
import com.example.FuelMemo.DieselPrice.Repository.DieselPriceRepository;
import com.example.FuelMemo.MasterData.Entity.District;
import com.example.FuelMemo.MasterData.Repository.DistrictRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service

public class FuelPriceService {

    public FuelPriceService(DistrictRepository districtRepository, DieselPriceRepository dieselPriceRepository) {
        this.districtRepository = districtRepository;
        this.dieselPriceRepository = dieselPriceRepository;
    }

    private final DistrictRepository districtRepository;
    private final DieselPriceRepository dieselPriceRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    // ✅ Correct API URL
    private static final String API_URL =
            "https://india-fuel-price-petrol-diesel-rates-by-state-district.p.rapidapi.com/v1/district";

    private static final String API_KEY = "a883c56339msh5d4c36dfe6eb6d3p1fbbc7jsn051049249e7f";

    // 🔁 Scheduler (Daily)
    @Scheduled(cron = "0 0 8 * * ?")
    public void fetchFuelPricesDaily() {

        List<District> districts = districtRepository.findAll();

        for (District d : districts) {

            String districtName = (d.getApiCityName() != null && !d.getApiCityName().isEmpty())
                    ? d.getApiCityName()
                    : d.getCityName();

            String stateName = d.getState().getStateName();

            try {
                FuelApiResponse response =
                        callApiWithRetry(stateName, districtName, 3);

                if (response != null) {
                    savePrice(d, response);
                    System.out.println("✅ Saved: " + districtName);
                } else {
                    fallbackSave(d);
                }

            } catch (Exception e) {
                System.out.println("❌ Error: " + districtName + " | " + e.getMessage());
                fallbackSave(d);
            }
        }
    }

    // 🔁 API Call with Retry
    private FuelApiResponse callApiWithRetry(String state, String district, int maxRetry) {

        for (int i = 1; i <= maxRetry; i++) {
            try {

                System.out.println("📡 Calling API: " + state + " - " + district);

                HttpHeaders headers = new HttpHeaders();
                headers.set("X-RapidAPI-Key", API_KEY);
                headers.set("X-RapidAPI-Host",
                        "india-fuel-price-petrol-diesel-rates-by-state-district.p.rapidapi.com");

                HttpEntity<String> entity = new HttpEntity<>(headers);

                String url = API_URL + "?state={state}&district={district}";

                ResponseEntity<FuelApiResponse> response =
                        restTemplate.exchange(
                                url,
                                HttpMethod.GET,
                                entity,
                                FuelApiResponse.class,
                                state,
                                district
                        );

                return response.getBody();

            } catch (Exception e) {
                System.out.println("⚠️ Retry " + i + " failed: " + e.getMessage());
            }
        }

        return null;
    }

    // 💾 Save Data
    private void savePrice(District d, FuelApiResponse res) {

        boolean exists = dieselPriceRepository
                .existsByDistrict_DistrictIdAndDate(
                        d.getDistrictId(), LocalDate.now());

        if (exists) return;

        try {
            // ✅ Remove ₹ if present
            String petrol = res.getPetrol().replace("₹", "").trim();
            String diesel = res.getDiesel().replace("₹", "").trim();

            DieselPrice price = new DieselPrice();
            price.setDistrict(d);
            price.setState(d.getState());
            price.setDate(LocalDate.now());

            price.setPetrol(new BigDecimal(petrol));
            price.setDiesel(new BigDecimal(diesel));

            dieselPriceRepository.save(price);

        } catch (Exception e) {
            System.out.println("❌ Parsing Error: " + e.getMessage());
            fallbackSave(d);
        }
    }

    // 🆘 Fallback Save
    private void fallbackSave(District d) {

        boolean exists = dieselPriceRepository
                .existsByDistrict_DistrictIdAndDate(
                        d.getDistrictId(), LocalDate.now());

        if (exists) return;

        DieselPrice price = new DieselPrice();

        price.setDistrict(d);
        price.setState(d.getState());
        price.setDate(LocalDate.now());

        price.setPetrol(BigDecimal.ZERO);
        price.setDiesel(BigDecimal.ZERO);

        dieselPriceRepository.save(price);

        System.out.println("⚠️ Fallback Saved: " + d.getCityName());
    }
}