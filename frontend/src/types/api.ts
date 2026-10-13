export type SkillType = 'TEACH' | 'LEARN';

export type ProficiencyLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';

export type SwapRequestStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED' | 'COMPLETED';

export interface UserResponse {
  id: number;
  name: string;
  email: string;
}

export interface LoginResponse {
  message: string;
  user: UserResponse;
}

export interface SkillResponse {
  id: number;
  name: string;
  description: string | null;
  createdAt: string;
  updatedAt: string | null;
}

export interface UserSkillResponse {
  id: number;
  skillId: number;
  skillName: string;
  skillDescription: string | null;
  skillType: SkillType;
  proficiencyLevel: ProficiencyLevel;
  description: string | null;
  createdAt: string;
}

export interface UserProfileResponse {
  userId: number;
  name: string;
  offeredSkills: UserSkillResponse[];
  learningSkills: UserSkillResponse[];
}

export interface OfferedSkillDiscoveryResponse {
  id: number;
  userId: number;
  userName: string;
  skillId: number;
  skillName: string;
  skillDescription: string | null;
  proficiencyLevel: ProficiencyLevel;
  description: string | null;
  createdAt: string;
}

export interface SkillSummaryDto {
  skillId: number;
  skillName: string;
  proficiencyLevel: ProficiencyLevel | null;
}

export interface SkillMatchResponse {
  matchedUser: UserResponse;
  skillsTheyTeachYou: SkillSummaryDto[];
  skillsYouTeachThem: SkillSummaryDto[];
}

export interface SwapRequestResponse {
  id: number;
  requester: UserResponse;
  recipient: UserResponse;
  offeredSkill: SkillSummaryDto;
  requestedSkill: SkillSummaryDto;
  status: SwapRequestStatus;
  message: string | null;
  createdAt: string;
  updatedAt: string | null;
}

export interface CreateSwapRequest {
  recipientId: number;
  offeredSkillId: number;
  requestedSkillId: number;
  message?: string;
}

export interface AddUserSkillRequest {
  skillId?: number;
  skillName?: string;
  proficiencyLevel: ProficiencyLevel;
  description?: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface ApiError {
  timestamp?: string;
  status: number;
  error: string;
  message: string;
  validationErrors?: Record<string, string>;
}
