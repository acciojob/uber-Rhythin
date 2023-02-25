package com.driver.services.impl;

import com.driver.model.*;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.*;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Customer customer=customerRepository2.findById(customerId).get();
		customerRepository2.delete(customer);

	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query

		Customer customer=customerRepository2.findById(customerId).get();

		List<Driver> driverList=driverRepository2.findAll();
		Driver driver=null;

		for(Driver d:driverList){
			if(d.getCab().isAvailable() && (driver==null || driver.getDiverId()>d.getDiverId())){
				driver=d;
			}
		}
		if(driver==null) throw new Exception("No cab available!");

		TripBooking tripBooking=new TripBooking();

		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setStatus(TripStatus.CONFIRMED);

		//bill to be set when booking or when completed?

		tripBooking.setDriver(driver);
		driver.getTripBookingList().add(tripBooking);

		driver.getCab().setAvailable(false);

		customer.getTripBookingList().add(tripBooking);
		tripBooking.setCustomer(customer);

		//will it cause duplicate when saving both customer and driver as tripbooking is chile of both
		driverRepository2.save(driver);
		customerRepository2.save(customer);

		return tripBooking;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.CANCELED);

		Driver driver=tripBooking.getDriver();
		Cab cab=driver.getCab();
		cab.setAvailable(true);
		driverRepository2.save(driver);

//		tripBookingRepository2.save(tripBooking);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.COMPLETED);


		Driver driver=tripBooking.getDriver();
		Cab cab=driver.getCab();
		cab.setAvailable(true);

		int bill=tripBooking.getDistanceInKm()*cab.getPerKmRate();
		tripBooking.setBill(bill);

		driverRepository2.save(driver);
	}
}
