export type MeetingStatus =
  | 'UPLOADED'
  | 'TRANSCRIBING'
  | 'SUMMARIZING'
  | 'COMPLETED'
  | 'FAILED';

export type ActionItemStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED';

export interface AuthResponse {
  token: string;
  tokenType: string;
  userId: number;
  fullName: string;
  email: string;
}

export interface MeetingSummary {
  id: number;
  title: string;
  status: MeetingStatus;
  actionItemCount: number;
  createdAt: string;
}

export interface ActionItem {
  id: number;
  owner: string;
  task: string;
  deadline: string | null;
  status: ActionItemStatus;
}

export interface MeetingDetail {
  id: number;
  title: string;
  originalFilename: string;
  status: MeetingStatus;
  errorMessage: string | null;
  transcript: string | null;
  overview: string | null;
  keyPoints: string[];
  followUpQuestions: string[];
  actionItems: ActionItem[];
  createdAt: string;
  updatedAt: string;
}

/** Statuses where the backend is still working and the UI should keep polling. */
export const IN_PROGRESS_STATUSES: MeetingStatus[] = [
  'UPLOADED',
  'TRANSCRIBING',
  'SUMMARIZING',
];
