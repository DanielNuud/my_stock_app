package daniel.nuud.currencyservice.controller;

import daniel.nuud.currencyservice.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/currency")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping("/{currency}")
    public Map<String, String> getCurrency(@PathVariable String currency) {
        return currencyService.getCurrencyRates(currency.toUpperCase());
    }

    @GetMapping("/{currency}/convert")
    public Double getCurrencyConvert(@PathVariable String currency, @RequestParam String toCurrency, @RequestParam Double amount) {
        return currencyService.convert(currency, toCurrency, amount);
    }
}
