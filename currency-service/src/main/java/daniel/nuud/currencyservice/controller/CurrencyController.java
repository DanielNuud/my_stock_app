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

    @GetMapping("/convert")
    public Double getCurrencyConvert(@RequestParam String from,
                                     @RequestParam String to,
                                     @RequestParam Double amount,
                                     @RequestHeader(value = "X-User-Key", required = false) String userKey) {
        if (userKey == null || userKey.isBlank()) userKey = "guest";
        return currencyService.convert(from, to, amount, userKey);
    }

}
