import { ActionItemStatus, MeetingStatus } from '../core/models/models';

export function statusLabel(status: MeetingStatus): string {
  switch (status) {
    case 'UPLOADED':
      return 'Queued';
    case 'TRANSCRIBING':
      return 'Transcribing';
    case 'SUMMARIZING':
      return 'Summarizing';
    case 'COMPLETED':
      return 'Ready';
    case 'FAILED':
      return 'Failed';
  }
}

export function statusClass(status: MeetingStatus): string {
  return `pill--${status.toLowerCase()}`;
}

export function actionStatusLabel(status: ActionItemStatus): string {
  switch (status) {
    case 'PENDING':
      return 'Pending';
    case 'IN_PROGRESS':
      return 'In progress';
    case 'COMPLETED':
      return 'Completed';
  }
}

export function actionStatusClass(status: ActionItemStatus): string {
  return `pill--${status.toLowerCase()}`;
}
