package com.udemy.libraryapi.api.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnedLoanDto {
    private Boolean returned;
}
