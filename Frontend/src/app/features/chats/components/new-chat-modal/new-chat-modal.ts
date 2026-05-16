import { Component, EventEmitter, OnInit, Output, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged, Subject, switchMap } from 'rxjs';
import { ChatApiService } from '../../../../core/services/chat-api.service';
import { ChatSearchUser, CreateGroupChatRequest } from '../../../../core/models/chat.model';
import { UserAvatarComponent } from '../../../../shared/components/user-avatar/user-avatar.component';

@Component({
  selector: 'app-new-chat-modal',
  imports: [FormsModule, UserAvatarComponent],
  templateUrl: './new-chat-modal.html',
})
export class NewChatModal implements OnInit {
  private readonly chatApi = inject(ChatApiService);

  @Output() closed = new EventEmitter<void>();
  @Output() chatCreated = new EventEmitter<string>();

  readonly searchQuery = signal('');
  readonly users = signal<ChatSearchUser[]>([]);
  readonly selected = signal<ChatSearchUser[]>([]);
  readonly groupName = signal('');
  readonly loading = signal(false);
  readonly creating = signal(false);
  readonly errorMessage = signal('');

  private readonly search$ = new Subject<string>();

  ngOnInit(): void {
    this.search$.pipe(
      debounceTime(250),
      distinctUntilChanged(),
      switchMap((q) => {
        this.loading.set(true);
        return this.chatApi.searchUsers(q || undefined);
      }),
    ).subscribe({
      next: (users) => { this.users.set(users); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
    this.search$.next('');
  }

  onQueryChange(q: string): void {
    this.searchQuery.set(q);
    this.search$.next(q);
  }

  isSelected(user: ChatSearchUser): boolean {
    return this.selected().some((s) => s.id === user.id);
  }

  toggleUser(user: ChatSearchUser): void {
    if (this.isSelected(user)) {
      this.selected.update((s) => s.filter((x) => x.id !== user.id));
    } else if (this.selected().length < 9) {
      this.selected.update((s) => [...s, user]);
    }
  }

  create(): void {
    const sel = this.selected();
    if (sel.length === 0 || this.creating()) return;
    this.creating.set(true);
    this.errorMessage.set('');

    const request: CreateGroupChatRequest = {
      usernames: sel.map((u) => u.username),
      nombre: this.groupName().trim() || undefined,
    };

    this.chatApi.createGroupChat(request).subscribe({
      next: (chat) => {
        this.creating.set(false);
        this.chatCreated.emit(chat.publicId);
      },
      error: () => { this.errorMessage.set('Error creando el chat.'); this.creating.set(false); },
    });
  }
}
