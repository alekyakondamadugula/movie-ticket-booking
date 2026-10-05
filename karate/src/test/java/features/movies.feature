Feature: Movie API

  Scenario: List movies
    Given url movieUrl
    And path 'movies'
    When method get
    Then status 200
