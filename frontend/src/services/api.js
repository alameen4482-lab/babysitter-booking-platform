/**
 * API client interacting with the Spring Boot backend.
 * Endpoints are matched directly against AuthController and BabysitterController.
 */

const TOKEN_KEY = 'kindred_auth_token';
const USER_KEY = 'kindred_user_data';

export const authStorage = {
  getToken: () => localStorage.getItem(TOKEN_KEY),
  setToken: (token) => {
    if (token) localStorage.setItem(TOKEN_KEY, token);
    else localStorage.removeItem(TOKEN_KEY);
  },
  getUser: () => {
    try {
      const data = localStorage.getItem(USER_KEY);
      return data ? JSON.parse(data) : null;
    } catch {
      return null;
    }
  },
  setUser: (user) => {
    if (user) localStorage.setItem(USER_KEY, JSON.stringify(user));
    else localStorage.removeItem(USER_KEY);
  },
  clear: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  },
};

/**
 * Universal request wrapper for backend communication.
 * Handles the standard ApiResponse { success, message, data } envelope.
 */
async function request(endpoint, options = {}) {
  const token = authStorage.getToken();
  
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(endpoint, {
    ...options,
    headers,
  });

  let payload = null;
  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    payload = await response.json();
  }

  if (!response.ok) {
    const errorMsg =
      (payload && (payload.message || payload.error)) ||
      `Request failed with status ${response.status}`;
    throw new Error(errorMsg);
  }

  return payload;
}

// ==========================================
// Auth Endpoints (AuthController)
// ==========================================

export async function loginUser(email, password) {
  // POST /auth/login
  const response = await request('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  });

  if (response && response.data) {
    const { token, ...userData } = response.data;
    authStorage.setToken(token);
    authStorage.setUser(userData);
  }

  return response;
}

export async function registerUser({ name, email, password, role }) {
  // POST /auth/register
  const response = await request('/auth/register', {
    method: 'POST',
    body: JSON.stringify({ name, email, password, role }),
  });

  if (response && response.data) {
    const { token, ...userData } = response.data;
    authStorage.setToken(token);
    authStorage.setUser(userData);
  }

  return response;
}

// ==========================================
// Babysitters Endpoints (BabysitterController)
// ==========================================

export async function getBabysitters() {
  // GET /api/babysitters
  return request('/api/babysitters', {
    method: 'GET',
  });
}

export async function searchBabysitters(params = {}) {
  // GET /api/babysitters/search
  const query = new URLSearchParams();
  if (params.startTime) query.append('startTime', params.startTime);
  if (params.endTime) query.append('endTime', params.endTime);
  if (params.minimumRating) query.append('minimumRating', params.minimumRating);

  const queryString = query.toString() ? `?${query.toString()}` : '';
  return request(`/api/babysitters/search${queryString}`, {
    method: 'GET',
  });
}

export async function getBabysitterById(id) {
  // GET /api/babysitters/{id}
  return request(`/api/babysitters/${id}`, {
    method: 'GET',
  });
}

// ==========================================
// Availability Slots (AvailabilitySlotController)
// ==========================================

export async function getAvailabilitySlots(babysitterId, startTime, endTime) {
  // GET /api/availability-slots?babysitterId={id}
  const query = new URLSearchParams();
  if (babysitterId) query.append('babysitterId', babysitterId);
  if (startTime) query.append('startTime', startTime);
  if (endTime) query.append('endTime', endTime);

  const queryString = query.toString() ? `?${query.toString()}` : '';
  return request(`/api/availability-slots${queryString}`, {
    method: 'GET',
  });
}

// ==========================================
// Bookings (BookingController)
// ==========================================

export async function createBooking({ babysitterId, slotId, notes }) {
  // POST /api/bookings
  return request('/api/bookings', {
    method: 'POST',
    body: JSON.stringify({
      babysitterId: Number(babysitterId),
      slotId: Number(slotId),
      notes: notes || null,
    }),
  });
}

export async function getMyBookings() {
  // GET /api/bookings/my
  return request('/api/bookings/my', {
    method: 'GET',
  });
}

export async function getBookingById(id) {
  // GET /api/bookings/{id}
  return request(`/api/bookings/${id}`, {
    method: 'GET',
  });
}
