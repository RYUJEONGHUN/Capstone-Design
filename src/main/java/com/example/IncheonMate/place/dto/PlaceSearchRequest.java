package com.example.IncheonMate.place.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceSearchRequest{
        private String location;
        private String category;
        private String vibe;
        private String companion;
        private Boolean isItinerary;
}