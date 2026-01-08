package com.example.IncheonMate.route.dto;

public class RouteRequest {

    public record RouteSearchRequest(
            String sx,
            String sy,
            String ex,
            String ey,
            String departureName,
            String arrivalName
    ){}
}
