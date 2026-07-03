import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ActionItem,
  ActionItemStatus,
  MeetingDetail,
  MeetingSummary,
} from '../models/models';

@Injectable({ providedIn: 'root' })
export class MeetingService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/meetings`;

  upload(file: File, title: string): Observable<MeetingDetail> {
    const form = new FormData();
    form.append('file', file);
    if (title.trim()) {
      form.append('title', title.trim());
    }
    return this.http.post<MeetingDetail>(`${this.base}/upload`, form);
  }

  list(): Observable<MeetingSummary[]> {
    return this.http.get<MeetingSummary[]>(this.base);
  }

  get(id: number): Observable<MeetingDetail> {
    return this.http.get<MeetingDetail>(`${this.base}/${id}`);
  }

  search(keyword: string): Observable<MeetingSummary[]> {
    const params = { keyword };
    return this.http.get<MeetingSummary[]>(`${this.base}/search`, { params });
  }

  updateActionItemStatus(
    itemId: number,
    status: ActionItemStatus,
  ): Observable<ActionItem> {
    return this.http.put<ActionItem>(
      `${environment.apiUrl}/action-items/${itemId}/status`,
      { status },
    );
  }

  sendEmail(meetingId: number, recipients: string[]): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${this.base}/${meetingId}/send-email`,
      { recipients },
    );
  }
}
