Feature: User API

  Scenario: List users
    Given url userUrl
    And path 'users'
    When method get
    Then status 200
