import {
  AddUserSkillRequest,
  ApiError,
  CreateSwapRequest,
  LoginResponse,
  OfferedSkillDiscoveryResponse,
  Page,
  SkillMatchResponse,
  SkillResponse,
  SwapRequestResponse,
  UserProfileResponse,
  UserResponse,
  UserSkillResponse,
} from '../types/api';

const rawBaseUrl = (import.meta.env.VITE_API_BASE_URL || '').trim();
const API_BASE_URL = rawBaseUrl.endsWith('/') ? rawBaseUrl.slice(0, -1) : rawBaseUrl;

class ApiClient {
  private getAuthHeader(): string | null {
    if (typeof localStorage === 'undefined') return null;
    return localStorage.getItem('skillswap_auth');
  }

  setAuthHeader(header: string | null) {
    if (typeof localStorage === 'undefined') return;
    if (header) {
      localStorage.setItem('skillswap_auth', header);
    } else {
      localStorage.removeItem('skillswap_auth');
    }
  }

  private getCsrfTokenFromCookie(): string | null {
    if (typeof document === 'undefined') return null;
    const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/);
    return match ? decodeURIComponent(match[1]) : null;
  }

  private async request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    const cleanEndpoint = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;
    const url = `${API_BASE_URL}${cleanEndpoint}`;
    const headers: Record<string, string> = {
      Accept: 'application/json',
      'X-Requested-With': 'XMLHttpRequest',
      ...(options.headers as Record<string, string>),
    };

    const authHeader = this.getAuthHeader();
    if (authHeader && !headers['Authorization']) {
      headers['Authorization'] = authHeader;
    }

    const csrfToken = this.getCsrfTokenFromCookie();
    if (csrfToken && options.method && !['GET', 'HEAD', 'OPTIONS'].includes(options.method.toUpperCase())) {
      headers['X-XSRF-TOKEN'] = csrfToken;
    }

    if (options.body && typeof options.body === 'string') {
      headers['Content-Type'] = 'application/json';
    }

    let response: Response;
    try {
      response = await fetch(url, {
        ...options,
        headers,
        credentials: 'include', // Standard HttpOnly session cookie
      });
    } catch (networkErr: any) {
      throw {
        status: 0,
        error: 'Network Error',
        message: `Failed to connect to backend (${url || 'relative API'}). If the backend service is starting or idle on Render, please allow ~30 seconds for it to wake up and try again.`,
      } as ApiError;
    }

    if (response.status === 204) {
      return {} as T;
    }

    const text = await response.text();
    let data: any = null;
    if (text) {
      try {
        data = JSON.parse(text);
      } catch {
        data = text;
      }
    }

    if (!response.ok) {
      const isHtml = typeof data === 'string' && data.trim().startsWith('<');
      const errorMessage = isHtml
        ? `Backend returned ${response.status} ${response.statusText} (${url}). Please verify that VITE_API_BASE_URL points to the correct Render service URL.`
        : (data?.message || data?.error || (typeof data === 'string' ? data : 'An unexpected error occurred'));

      const error: ApiError = {
        status: response.status,
        error: response.statusText,
        message: errorMessage,
        validationErrors: data?.validationErrors,
        timestamp: data?.timestamp,
      };
      throw error;
    }

    return data as T;
  }

  // --- Auth Endpoints ---
  async register(data: { name: string; email: string; password: string }): Promise<UserResponse> {
    const authHeader = 'Basic ' + btoa(`${data.email.trim().toLowerCase()}:${data.password}`);
    const response = await this.request<UserResponse>('/api/auth/register', {
      method: 'POST',
      headers: {
        Authorization: authHeader,
      },
      body: JSON.stringify(data),
    });
    this.setAuthHeader(authHeader);
    return response;
  }

  async login(data: { email: string; password: string }): Promise<LoginResponse> {
    const authHeader = 'Basic ' + btoa(`${data.email.trim().toLowerCase()}:${data.password}`);
    const response = await this.request<LoginResponse>('/api/auth/login', {
      method: 'POST',
      headers: {
        Authorization: authHeader,
      },
      body: JSON.stringify(data),
    });
    this.setAuthHeader(authHeader);
    return response;
  }

  async getCurrentUser(): Promise<UserResponse> {
    return this.request<UserResponse>('/api/auth/me');
  }

  async logout(): Promise<void> {
    try {
      await this.request<void>('/api/auth/logout', {
        method: 'POST',
      });
    } finally {
      this.setAuthHeader(null);
    }
  }

  // --- Skills Catalog ---
  async searchSkills(name?: string, page = 0, size = 10): Promise<Page<SkillResponse>> {
    const params = new URLSearchParams();
    if (name) params.append('name', name);
    params.append('page', page.toString());
    params.append('size', size.toString());
    return this.request<Page<SkillResponse>>(`/api/skills?${params.toString()}`);
  }

  async getSkillById(id: number): Promise<SkillResponse> {
    return this.request<SkillResponse>(`/api/skills/${id}`);
  }

  // --- My Skills (Offered / Learning) ---
  async getMySkills(): Promise<UserSkillResponse[]> {
    return this.request<UserSkillResponse[]>('/api/users/me/skills');
  }

  async getMyOfferedSkills(): Promise<UserSkillResponse[]> {
    return this.request<UserSkillResponse[]>('/api/users/me/skills/offered');
  }

  async addMyOfferedSkill(data: AddUserSkillRequest): Promise<UserSkillResponse> {
    return this.request<UserSkillResponse>('/api/users/me/skills/offered', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async removeMyOfferedSkill(skillId: number): Promise<void> {
    return this.request<void>(`/api/users/me/skills/offered/${skillId}`, {
      method: 'DELETE',
    });
  }

  async getMyLearningSkills(): Promise<UserSkillResponse[]> {
    return this.request<UserSkillResponse[]>('/api/users/me/skills/learning');
  }

  async addMyLearningSkill(data: AddUserSkillRequest): Promise<UserSkillResponse> {
    return this.request<UserSkillResponse>('/api/users/me/skills/learning', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async removeMyLearningSkill(skillId: number): Promise<void> {
    return this.request<void>(`/api/users/me/skills/learning/${skillId}`, {
      method: 'DELETE',
    });
  }

  // --- Public Profile ---
  async getUserProfile(userId: number): Promise<UserProfileResponse> {
    return this.request<UserProfileResponse>(`/api/users/${userId}/profile`);
  }

  // --- Marketplace Discovery & Matchmaking ---
  async discoverOfferedSkills(name?: string, page = 0, size = 10): Promise<Page<OfferedSkillDiscoveryResponse>> {
    const params = new URLSearchParams();
    if (name) params.append('name', name);
    params.append('page', page.toString());
    params.append('size', size.toString());
    return this.request<Page<OfferedSkillDiscoveryResponse>>(`/api/skills/offered?${params.toString()}`);
  }

  async getComplementaryMatches(): Promise<SkillMatchResponse[]> {
    return this.request<SkillMatchResponse[]>('/api/skills/matches');
  }

  // --- Swap Requests ---
  async createSwapRequest(data: CreateSwapRequest): Promise<SwapRequestResponse> {
    return this.request<SwapRequestResponse>('/api/swaps', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  }

  async getIncomingSwapRequests(): Promise<SwapRequestResponse[]> {
    return this.request<SwapRequestResponse[]>('/api/swaps/incoming');
  }

  async getOutgoingSwapRequests(): Promise<SwapRequestResponse[]> {
    return this.request<SwapRequestResponse[]>('/api/swaps/outgoing');
  }

  async acceptSwapRequest(id: number): Promise<SwapRequestResponse> {
    return this.request<SwapRequestResponse>(`/api/swaps/${id}/accept`, {
      method: 'PUT',
    });
  }

  async rejectSwapRequest(id: number): Promise<SwapRequestResponse> {
    return this.request<SwapRequestResponse>(`/api/swaps/${id}/reject`, {
      method: 'PUT',
    });
  }

  async cancelSwapRequest(id: number): Promise<SwapRequestResponse> {
    return this.request<SwapRequestResponse>(`/api/swaps/${id}/cancel`, {
      method: 'PUT',
    });
  }
}

export const api = new ApiClient();
