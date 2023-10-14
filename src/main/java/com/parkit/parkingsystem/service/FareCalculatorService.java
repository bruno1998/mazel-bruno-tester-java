package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    private double freeParkingLimitTimeInHour = 0.5;

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }

    public void calculateFare(Ticket ticket, boolean discount) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        int inHour = (int) ticket.getInTime().getTime();
        int outHour = (int) ticket.getOutTime().getTime();

        // TODO: Some tests are failing here. Need to check if this logic is correct
        double duration = (double) (outHour - inHour) / 1000 / 60 / 60;

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                if (freeParkingUnderHalfAnHour(duration)) {
                    ticket.setPrice(0);
                } else {
                    if (discount) {
                        ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR * 0.95);
                    } else {
                        ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                    }
                }
                break;
            }
            case BIKE: {
                if (freeParkingUnderHalfAnHour(duration)) {
                    ticket.setPrice(0);
                } else {
                    if (discount) {
                        ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR * 0.95);
                    } else {
                        ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                    }
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Unkown Parking Type");
        }
    }

    private boolean freeParkingUnderHalfAnHour(double duration) {
        if (duration >= freeParkingLimitTimeInHour) {
            return false;
        } else {
            return true;
        }
    }
}