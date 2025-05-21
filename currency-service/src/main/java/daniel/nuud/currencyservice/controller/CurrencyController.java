package daniel.nuud.currencyservice.controller;

import daniel.nuud.currencyservice.service.CurrencyService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/currency")
public class CurrencyController {

    private CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping("/{currency}")
    public Map<String, String> getCurrency(@PathVariable String currency) {
        return currencyService.getCurrencyRates(currency.toUpperCase());
    }

    @GetMapping("/{currency}/convert")
    public Double getCurrencyConvert(@PathVariable String currency, @RequestParam String toCurrency, @RequestParam Double amount) {
        return currencyService.convert(currency, toCurrency, amount);
    }
}
