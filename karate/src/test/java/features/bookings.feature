Feature: Booking API

  Scenario: List bookings for a user
    Given url bookingUrl
    And path 'bookings', 'user', 42
    When method get
    Then status 200
