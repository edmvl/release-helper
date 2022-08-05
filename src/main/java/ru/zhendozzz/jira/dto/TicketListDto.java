package ru.zhendozzz.jira.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TicketListDto {
    private List<TicketDto> ticketDtoList;
}
