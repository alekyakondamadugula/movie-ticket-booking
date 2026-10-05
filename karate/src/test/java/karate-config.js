function fn() {
  return {
    userUrl: karate.properties['user.url'] || 'http://localhost:8081',
    movieUrl: karate.properties['movie.url'] || 'http://localhost:8082',
    bookingUrl: karate.properties['booking.url'] || 'http://localhost:8083'
  };
}
