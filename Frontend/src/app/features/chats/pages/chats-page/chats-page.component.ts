import { Component, OnInit, inject, signal } from '@angular/core';

import { FormsModule } from '@angular/forms';

import { ActivatedRoute, Router } from '@angular/router';

import { ChatApiService } from '../../../../core/services/chat-api.service';

import { CurrentUserService } from '../../../../core/services/current-user.service';

import { ChatListItem as ChatListItemModel, ChatDetail as ChatDetailModel } from '../../../../core/models/chat.model';

import { ChatListItem as ChatListItemComponent } from '../../components/chat-list-item/chat-list-item';

import { ChatDetail as ChatDetailComponent } from '../../components/chat-detail/chat-detail';

import { NewChatModal } from '../../components/new-chat-modal/new-chat-modal';



const PUBLIC_ID_PATTERN = /^[A-Za-z0-9_-]{10,21}$/;



@Component({

  selector: 'app-chats-page',

  standalone: true,

  imports: [FormsModule, ChatListItemComponent, ChatDetailComponent, NewChatModal],

  templateUrl: './chats-page.component.html',

})

export class ChatsPageComponent implements OnInit {

  private readonly chatApi = inject(ChatApiService);

  private readonly currentUser = inject(CurrentUserService);

  private readonly route = inject(ActivatedRoute);

  private readonly router = inject(Router);



  readonly chats = signal<ChatListItemModel[]>([]);

  readonly loading = signal(false);

  readonly activeChatPublicId = signal<string | null>(null);

  readonly activeChat = signal<ChatDetailModel | null>(null);

  readonly showNewModal = signal(false);



  readonly myUsername = this.currentUser.profile;



  ngOnInit(): void {

    this.loadChats();

    this.route.queryParamMap.subscribe((params) => {

      const idParam = params.get('id');

      if (!idParam) {

        this.closeChat(false);

        return;

      }

      if (!PUBLIC_ID_PATTERN.test(idParam)) {

        this.closeChat(true);

        return;

      }

      this.openChatByPublicId(idParam);

    });

  }



  loadChats(): void {

    this.loading.set(true);

    this.chatApi.getMisChats().subscribe({

      next: (data) => { this.chats.set(data); this.loading.set(false); },

      error: () => this.loading.set(false),

    });

  }



  openChatByPublicId(publicId: string): void {

    if (this.activeChatPublicId() === publicId && this.activeChat()) return;

    this.activeChatPublicId.set(publicId);

    this.activeChat.set(null);

    this.chatApi.getChatDetail(publicId).subscribe({

      next: (detail) => {

        this.activeChat.set(detail);

        this.router.navigate([], { queryParams: { id: publicId }, replaceUrl: true });

      },

      error: () => this.closeChat(true),

    });

  }



  closeChat(updateUrl = true): void {

    this.activeChatPublicId.set(null);

    this.activeChat.set(null);

    if (updateUrl) {

      this.router.navigate(['/app/chats'], { replaceUrl: true });

    }

  }



  selectChat(chat: ChatListItemModel): void {

    this.openChatByPublicId(chat.publicId);

  }



  onChatUpdated(): void {

    this.loadChats();

  }



  onChatLeft(): void {

    this.closeChat();

    this.loadChats();

  }



  onChatDetailUpdated(detail: ChatDetailModel): void {

    this.activeChat.set(detail);

    this.loadChats();

  }



  onNewChatCreated(publicId: string): void {

    this.showNewModal.set(false);

    this.loadChats();

    this.openChatByPublicId(publicId);

  }

}

