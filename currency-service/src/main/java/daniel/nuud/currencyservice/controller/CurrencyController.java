package daniel.nuud.currencyservice.controller;

import daniel.nuud.currencyservice.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/currency")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    @Operation(
            summary = "Convert currency amount",
            description = "Converts the given amount from one currency to another using the latest FX rate. "
                    + "Returns a numeric value in the target currency."
    )
    @GetMapping("/convert")
    public Double getCurrencyConvert(
            @Parameter(
                    description = "Source currency (ISO-4217, uppercase).",
                    example = "USD",
                    schema = @Schema(minLength = 3, maxLength = 3, pattern = "^[A-Z]{3}$")
            )
            @RequestParam String from,

            @Parameter(
                    description = "Target currency (ISO-4217, uppercase).",
                    example = "EUR",
                    schema = @Schema(minLength = 3, maxLength = 3, pattern = "^[A-Z]{3}$")
            )
            @RequestParam String to,

            @Parameter(
                    description = "Amount to convert.",
                    example = "100.00",
                    schema = @Schema(type = "number", format = "double", minimum = "0")
            )
            @RequestParam Double amount,

            @Parameter(
                    in = ParameterIn.HEADER,
                    name = "X-User-Key",
                    description = "Optional user key for tracking/rate limits. Defaults to 'guest'.",
                    example = "u123",
                    required = false
            )
            @RequestHeader(value = "X-User-Key", defaultValue = "guest", required = false) String userKey
    ) {
        return currencyService.convert(from, to, amount, userKey);
    }

}
