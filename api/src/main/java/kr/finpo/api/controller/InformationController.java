package kr.finpo.api.controller;

import kr.finpo.api.dto.DataResponse;
import kr.finpo.api.repository.InformationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
@RequestMapping("/information")
public class InformationController {

    private final InformationRepository informationRepository;

    @GetMapping("/{key}")
    public DataResponse<Object> get(@PathVariable String key) {
        return DataResponse.of(informationRepository.findByTypeAndHidden(key, false));
    }
}
