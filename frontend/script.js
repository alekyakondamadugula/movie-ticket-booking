// API Configuration for environment-aware deployment
// On localhost (port 5500): uses http://localhost:8081/8082/8083
// On Render or other deployed environments: uses the actual service URLs

const isLocal = location.hostname === 'localhost' || location.hostname === '127.0.0.1';

const API = isLocal ? {
  users: 'http://localhost:8081/users',
  movies: 'http://localhost:8082/movies',
  bookings: 'http://localhost:8083/bookings'
} : {
  users: 'https://movie-ticket-booking-2-fzm2.onrender.com/users',
  movies: 'https://movie-ticket-booking-1-pf6t.onrender.com/movies',
  bookings: 'https://booking-service.onrender.com/bookings'  // Update after booking service is deployed
};

const MOVIE_POSTERS = {
  pushpa2: 'https://image.tmdb.org/t/p/w500/kcZbrTWfvo2BS0exASpIOv8mZIC.jpg',
  rrr: 'https://m.media-amazon.com/images/M/MV5BODUwNDNjYzctODUxNy00ZTA2LWIyYTEtMDc5Y2E5ZjBmNTMzXkEyXkFqcGdeQXVyODE5NzE3OTE%40._V1_FMjpg_UX1000_.jpg',
  kalki2898ad: 'https://m.media-amazon.com/images/M/MV5BMTM3ZGUwYTEtZTI5NS00ZmMyLTk2YmQtMWU4YjlhZTI3NjRjXkEyXkFqcGc%40._V1_.jpg',
  hinanna: 'https://m.media-amazon.com/images/M/MV5BZmNiOGZjMDYtYThiZS00ZmZhLWE0NjctNGIwMWZmNDkyYzQ1XkEyXkFqcGc%40._V1_FMjpg_UX1000_.jpg',
  devara: 'https://m.media-amazon.com/images/M/MV5BZWEwNmYwYTAtMmQxYS00ZTgwLWE0NmUtNGIwZDEyZmYwN2EwXkEyXkFqcGc%40._V1_FMjpg_UX1000_.jpg'
};

const movieList = document.querySelector('#movie-list');
const movieSelect = document.querySelector('#booking-movie');
const historyList = document.querySelector('#booking-history');
let movies = [];
let bookings = [];
let authMode = 'login';
let currentUser = readSession();
let historyRequest = 0;
let selectedSeats = [];
let seatConfiguration = null;
let seatConfigurationPromise = null;

async function apiRequest(url, options = {}) {
  const response = await fetch(url, {
    ...options,
    mode: 'cors',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json', ...(options.headers || {}) }
  });
  const text = await response.text();
  let body = null;
  if (text) {
    try { body = JSON.parse(text); } catch { body = text; }
  }
  if (!response.ok) {
    const message = body && typeof body === 'object' ? body.message || body.error : body;
    throw new Error(message || `Request failed (${response.status})`);
  }
  return body;
}

function readSession() {
  const savedUser = localStorage.getItem('moviebook-user');
  if (savedUser) {
    try {
      const user = JSON.parse(savedUser);
      if (user.id) return { id: Number(user.id), name: user.name || 'MovieBook member' };
    } catch { localStorage.removeItem('moviebook-user'); }
  }
  const id = localStorage.getItem('moviebook-user-id');
  return id ? { id: Number(id), name: localStorage.getItem('moviebook-user-name') || 'MovieBook member' } : null;
}

function saveSession(user) {
  const userId = user?.id ?? user?.userId;
  if (!userId) throw new Error('The User Service response did not include a user ID.');
  currentUser = { id: Number(userId), name: user.name || 'MovieBook member' };
  localStorage.setItem('moviebook-user', JSON.stringify(currentUser));
  localStorage.setItem('moviebook-user-id', currentUser.id);
  localStorage.setItem('moviebook-user-name', currentUser.name);
  renderSession();
}

function clearSession() {
  localStorage.removeItem('moviebook-user');
  localStorage.removeItem('moviebook-user-id');
  localStorage.removeItem('moviebook-user-name');
  currentUser = null;
  bookings = [];
  historyRequest++;
  renderSession();
  historyList.innerHTML = '';
}

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, char => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  })[char]);
}

function showMessage(element, message, isError = false) {
  element.textContent = message;
  element.classList.toggle('error', isError);
}

function renderSession() {
  const loggedIn = Boolean(currentUser);
  document.querySelector('#auth-screen').classList.toggle('hidden', loggedIn);
  document.querySelector('#app-shell').classList.toggle('hidden', !loggedIn);
  document.querySelector('#session-area').innerHTML = loggedIn
    ? '<a href="#account">My account</a>'
    : '<a href="#auth-card">Login</a>';
  document.querySelector('#history-nav').classList.toggle('hidden', !loggedIn);
  document.querySelector('#welcome-card').classList.toggle('hidden', !loggedIn);
  document.querySelector('#welcome-title').textContent = loggedIn ? `Welcome, ${currentUser.name} 👋` : 'Welcome!';
  document.querySelector('#login-required').classList.toggle('hidden', loggedIn);
  document.querySelector('#history').classList.toggle('hidden', !loggedIn);
  const bookingForm = document.querySelector('#booking-form');
  bookingForm.classList.toggle('locked', !loggedIn);
  bookingForm.querySelectorAll('input, select, button').forEach(control => { control.disabled = !loggedIn; });
  if (typeof dateSelect !== 'undefined') dateSelect.disabled = !loggedIn || !movieSelect.value;
  if (typeof timeSelect !== 'undefined') timeSelect.disabled = !loggedIn || !dateSelect.value;
  document.querySelector('#signed-in-as').textContent = loggedIn ? `Booking as ${currentUser.name} (ID ${currentUser.id})` : '';
  if (loggedIn) {
    loadMovies();
    loadBookingHistory();
  }
}

document.querySelector('#logout-button').addEventListener('click', () => {
  clearSession();
  showMessage(document.querySelector('#auth-message'), 'You have been logged out.');
});

function setAuthMode(mode) {
  authMode = mode;
  const registering = mode === 'register';
  document.querySelectorAll('.auth-tab').forEach(tab => tab.classList.toggle('active', tab.dataset.mode === mode));
  document.querySelector('#name-field').classList.toggle('hidden', !registering);
  document.querySelector('#auth-name').required = registering;
  document.querySelector('#auth-title').textContent = registering ? 'Create your account' : 'Welcome to MovieBook';
  document.querySelector('#auth-subtitle').textContent = registering ? 'Join us for your next movie night' : 'Sign in to book your tickets';
  document.querySelector('#auth-submit').textContent = registering ? 'Register' : 'Login';
  document.querySelector('#auth-password').autocomplete = registering ? 'new-password' : 'current-password';
  showMessage(document.querySelector('#auth-message'), '');
}

document.querySelectorAll('.auth-tab').forEach(tab => tab.addEventListener('click', () => setAuthMode(tab.dataset.mode)));

document.querySelector('#auth-form').addEventListener('submit', async event => {
  event.preventDefault();
  const form = event.currentTarget;
  const formData = new FormData(form);
  const fields = authMode === 'register'
    ? { name: String(formData.get('name') || '').trim(), email: String(formData.get('email') || '').trim(), password: formData.get('password') }
    : { email: String(formData.get('email') || '').trim(), password: formData.get('password') };
  const message = document.querySelector('#auth-message');
  try {
    const user = await apiRequest(authMode === 'register' ? API.users : `${API.users}/login`, {
      method: 'POST', body: JSON.stringify(fields)
    });
    saveSession(user);
    form.reset();
  } catch (error) {
    const detail = error instanceof TypeError
      ? 'Could not reach User Service. Open the frontend at http://localhost:5500 and make sure the service is running on port 8081.'
      : error.message || 'Check your details and try again.';
    showMessage(message, detail, true);
  }
});

function renderMovies() {
  if (!movies.length) {
    movieList.innerHTML = '<div class="empty-state">No movies found in Movie Service.</div>';
    movieSelect.innerHTML = '<option value="">No movies available</option>';
    return;
  }
  movieSelect.innerHTML = '<option value="">Choose a movie</option>' + movies.map(movie =>
    `<option value="${escapeHtml(movie.id)}">${escapeHtml(movie.title)} · ID ${escapeHtml(movie.id)}</option>`
  ).join('');
  movieList.innerHTML = movies.map(movie => {
    const titleKey = String(movie.title || '').toLowerCase().replace(/[^a-z0-9]/g, '');
    const posterUrl = MOVIE_POSTERS[titleKey];
    return `
    <article class="movie-card" id="movie-${escapeHtml(movie.id)}">
      <div class="movie-poster">
        <img src="${posterUrl || ''}" alt="${escapeHtml(movie.title || 'Movie')} poster" loading="lazy">
        <div class="poster-fallback" aria-hidden="true"><span>✦</span><strong>${escapeHtml(movie.title || 'Now showing')}</strong><small>MOVIEBOOK</small></div>
      </div>
      <div class="movie-info">
        <div class="movie-title-line"><h3>${escapeHtml(movie.title || 'Untitled movie')}</h3><span class="rating">★ ${escapeHtml(movie.rating ?? '—')}</span></div>
        <div class="movie-meta"><span>${escapeHtml(movie.genre || 'Genre TBA')}</span><span>${escapeHtml(movie.language || 'Language TBA')}</span><span>${escapeHtml(movie.duration ?? '—')} min</span></div>
        <div class="movie-bottom"><span class="seats-note">● 48 seats · demo</span><button class="button book-now" type="button" data-movie-id="${escapeHtml(movie.id)}">Book Now →</button></div>
      </div>
    </article>`;
  }).join('');
  movieList.querySelectorAll('.movie-poster img').forEach(image => {
    const showFallback = () => {
      image.hidden = true;
      image.nextElementSibling.classList.add('visible');
    };
    image.addEventListener('error', showFallback, { once: true });
    if (!image.getAttribute('src')) showFallback();
  });
  movieList.querySelectorAll('.book-now').forEach(button => button.addEventListener('click', () => {
    movieSelect.value = button.dataset.movieId;
    resetShowSelection();
    document.querySelector('#booking').scrollIntoView({ behavior: 'smooth' });
    if (!currentUser) showMessage(document.querySelector('#booking-message'), 'Please login or register to book tickets.', true);
  }));
  renderBookingHistory();
}

async function loadMovies() {
  movieList.innerHTML = '<div class="empty-state">Loading movies…</div>';
  try {
    movies = await apiRequest(API.movies);
    renderMovies();
  } catch (error) {
    movieList.innerHTML = `<div class="empty-state">${escapeHtml(error.message)}<br>Make sure Movie Service is running.</div>`;
  }
}

document.querySelector('#load-movies').addEventListener('click', loadMovies);

document.querySelector('#booking-form').addEventListener('submit', async event => {
  event.preventDefault();
  const message = document.querySelector('#booking-message');
  if (!currentUser) {
    showMessage(message, 'Please login or register to book tickets.', true);
    return;
  }
  const data = Object.fromEntries(new FormData(event.currentTarget));
  const movie = movies.find(item => Number(item.id) === Number(data.movieId));
  if (!data.movieId || !data.showDate || !data.showTime || !selectedSeats.length) {
    showMessage(message, 'Choose a movie, date, showtime and at least one seat.', true);
    return;
  }
  data.userId = currentUser.id;
  data.movieId = Number(data.movieId);
  data.movieTitle = movie?.title || '';
  data.seatNumbers = [...selectedSeats];
  try {
    const booking = await apiRequest(API.bookings, { method: 'POST', body: JSON.stringify(data) });
    if (!bookings.some(item => Number(item.id) === Number(booking.id))) bookings.unshift(booking);
    renderBookingHistory();
    showMessage(message, `🎟️ Booking confirmed! Booking ID: ${booking.id}`);
    selectedSeats = [];
    await loadSeatAvailability();
    await loadBookingHistory();
    bookingSummary.classList.remove('hidden');
    bookingSummary.innerHTML = `<strong>Booking confirmed</strong><span>Movie: ${escapeHtml(booking.movieTitle || movie?.title || '')}</span><span>Date: ${escapeHtml(booking.showDate)}</span><span>Time: ${escapeHtml(booking.showTime)}</span><span>Seats: ${escapeHtml((booking.seatNumbers || []).join(', '))}</span><span>Status: ${escapeHtml(booking.status)}</span>`;
  } catch (error) {
    showMessage(message, error.message || 'Could not create booking. Please try again.', true);
  }
});

function renderBookingHistory() {
  if (!currentUser) {
    historyList.innerHTML = '';
    return;
  }
  if (!bookings.length) {
    historyList.innerHTML = '<div class="empty-state">No bookings yet. Choose a movie and book your first ticket.</div>';
    return;
  }
  historyList.innerHTML = bookings.map(booking => {
    const movie = movies.find(item => Number(item.id) === Number(booking.movieId));
    const title = booking.movieTitle || movie?.title || `Movie ${booking.movieId}`;
    const status = String(booking.status || 'CONFIRMED').toUpperCase();
    return `
      <article class="history-card" data-booking-id="${escapeHtml(booking.id)}">
        <div class="history-card-head"><div class="history-movie"><span class="history-film-icon">🎬</span><div><h3>${escapeHtml(title)}</h3><p>Booking ID: ${escapeHtml(booking.id)}</p></div></div><span class="status-badge ${status === 'CANCELLED' ? 'cancelled' : 'confirmed'}">${escapeHtml(status)}</span></div>
        <div class="history-details"><div><span>Date</span><strong>${escapeHtml(booking.showDate || '')}</strong></div><div><span>Show Time</span><strong>${escapeHtml(booking.showTime)}</strong></div><div><span>Seats</span><strong>${escapeHtml((booking.seatNumbers || []).join(', '))}</strong></div></div>
        ${status === 'CONFIRMED' ? `<button class="button cancel-button" type="button" data-cancel-id="${escapeHtml(booking.id)}">Cancel Booking</button>` : ''}
      </article>`;
  }).join('');
  historyList.querySelectorAll('[data-cancel-id]').forEach(button => button.addEventListener('click', () => cancelBooking(button.dataset.cancelId, button)));
}

async function loadBookingHistory() {
  if (!currentUser) return;
  const userId = currentUser.id;
  const requestId = ++historyRequest;
  if (!bookings.length) historyList.innerHTML = '<div class="empty-state">Loading your bookings…</div>';
  try {
    const result = await apiRequest(`${API.bookings}/user/${encodeURIComponent(userId)}`);
    if (!currentUser || currentUser.id !== userId || requestId !== historyRequest) return;
    bookings = Array.isArray(result) ? result : [];
    renderBookingHistory();
  } catch (error) {
    if (!currentUser || currentUser.id !== userId || requestId !== historyRequest) return;
    historyList.innerHTML = `<div class="empty-state error">Could not load booking history: ${escapeHtml(error.message)}</div>`;
  }
}

async function cancelBooking(bookingId, button) {
  if (!currentUser) return;
  button.disabled = true;
  try {
    const updated = await apiRequest(`${API.bookings}/${encodeURIComponent(bookingId)}/cancel`, { method: 'PUT' });
    bookings = bookings.map(booking => Number(booking.id) === Number(bookingId) ? updated : booking);
    renderBookingHistory();
    await loadSeatAvailability();
    await loadBookingHistory();
  } catch (error) {
    button.disabled = false;
    showMessage(document.querySelector('#booking-message'), error.message || 'Could not cancel this booking.', true);
  }
}

document.querySelector('#refresh-history').addEventListener('click', loadBookingHistory);

const dateSelect = document.querySelector('#show-date');
const timeSelect = document.querySelector('#show-time');
const seatPicker = document.querySelector('#seat-picker');
const bookingSummary = document.querySelector('#booking-summary');

function resetShowSelection() {
  dateSelect.disabled = !movieSelect.value;
  dateSelect.value = '';
  timeSelect.innerHTML = '<option value="">Select a time</option>';
  timeSelect.disabled = true;
  selectedSeats = [];
  bookingSummary.classList.add('hidden');
  seatPicker.innerHTML = '<p class="seat-picker-hint">Choose a date and showtime to view seats.</p>';
  if (movieSelect.value) loadSeatConfiguration().catch(() => {});
}

async function loadSeatConfiguration() {
  if (seatConfiguration) return seatConfiguration;
  if (seatConfigurationPromise) return seatConfigurationPromise;
  seatConfigurationPromise = apiRequest(`${API.bookings}/availability`).then(configuration => {
    if (!Array.isArray(configuration?.showDates) || !Array.isArray(configuration?.showTimes)) {
      throw new Error('Booking Service did not return showDates and showTimes.');
    }
    seatConfiguration = configuration;
    const selectedDate = dateSelect.value;
    dateSelect.innerHTML = '<option value="">Select a date</option>' + configuration.showDates.map(date =>
      `<option value="${escapeHtml(date)}">${escapeHtml(new Date(`${date}T00:00:00`).toLocaleDateString(undefined, { month: 'short', day: 'numeric', weekday: 'short' }))}</option>`
    ).join('');
    if (configuration.showDates.includes(selectedDate)) dateSelect.value = selectedDate;
    dateSelect.disabled = !currentUser || !movieSelect.value;
    return configuration;
  }).catch(error => {
    seatConfiguration = null;
    seatConfigurationPromise = null;
    seatPicker.innerHTML = `<p class="seat-picker-hint error">Could not load show dates: ${escapeHtml(error.message)}</p>`;
    throw error;
  });
  return seatConfigurationPromise;
}

async function loadSeatAvailability() {
  const movieId = movieSelect.value;
  const showDate = dateSelect.value;
  const showTime = timeSelect.value;
  if (!movieId || !showDate || !showTime) return;
  selectedSeats = [];
  seatPicker.innerHTML = '<p class="seat-picker-hint">Loading seats…</p>';
  try {
    const params = new URLSearchParams({ movieId, showDate, showTime });
    const availability = await apiRequest(`${API.bookings}/availability?${params}`);
    seatConfiguration = availability;
    renderSeatPicker(availability);
  } catch (error) {
    seatPicker.innerHTML = `<p class="seat-picker-hint error">Could not load seats: ${escapeHtml(error.message)}</p>`;
  }
}

function renderSeatPicker(availability) {
  const booked = new Set(availability.bookedSeats || []);
  seatPicker.innerHTML = `<div class="screen-label">SCREEN</div><div class="seat-grid">${availability.seats.map(seat => {
    const state = booked.has(seat) ? 'booked' : selectedSeats.includes(seat) ? 'selected' : 'available';
    return `<button class="seat ${state}" type="button" data-seat="${seat}" aria-label="Seat ${seat} ${state}" ${state === 'booked' ? 'disabled' : ''}>${seat}</button>`;
  }).join('')}</div><div class="seat-legend"><span><i class="available"></i>Available</span><span><i class="selected"></i>Selected</span><span><i class="booked"></i>Booked</span></div><p class="selected-seats">Selected Seats: <strong>${selectedSeats.join(', ') || 'None'}</strong> <span>Total Seats: ${selectedSeats.length}</span></p>`;
  seatPicker.querySelectorAll('[data-seat]').forEach(button => button.addEventListener('click', () => {
    selectedSeats = selectedSeats.includes(button.dataset.seat)
      ? selectedSeats.filter(seat => seat !== button.dataset.seat)
      : [...selectedSeats, button.dataset.seat];
    renderSeatPicker(availability);
  }));
  renderSummary();
}

function renderSummary() {
  const movie = movies.find(item => Number(item.id) === Number(movieSelect.value));
  if (!movie || !dateSelect.value || !timeSelect.value || !selectedSeats.length) {
    bookingSummary.classList.add('hidden');
    return;
  }
  bookingSummary.classList.remove('hidden');
  bookingSummary.innerHTML = `<strong>Booking summary</strong><span>Movie: ${escapeHtml(movie.title)}</span><span>Date: ${escapeHtml(dateSelect.value)}</span><span>Time: ${escapeHtml(timeSelect.value)}</span><span>Seats: ${escapeHtml(selectedSeats.join(', '))}</span>`;
}

movieSelect.addEventListener('change', resetShowSelection);
dateSelect.addEventListener('change', async () => {
  selectedSeats = [];
  try {
    const configuration = await loadSeatConfiguration();
    timeSelect.innerHTML = '<option value="">Select a time</option>' + configuration.showTimes.map(time =>
      `<option value="${escapeHtml(time)}">${escapeHtml(time)}</option>`
    ).join('');
  } catch {
    timeSelect.innerHTML = '<option value="">Showtimes unavailable</option>';
    timeSelect.disabled = true;
    return;
  }
  timeSelect.disabled = false;
  seatPicker.innerHTML = '<p class="seat-picker-hint">Choose a showtime to view seats.</p>';
  bookingSummary.classList.add('hidden');
});
timeSelect.addEventListener('change', loadSeatAvailability);
loadSeatConfiguration().catch(() => {});
renderSession();
