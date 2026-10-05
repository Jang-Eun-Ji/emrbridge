import { Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatSidenavModule } from '@angular/material/sidenav';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

interface NavItem {
  path: string;
  label: string;
  caption: string;
  icon: string;
}

@Component({
  selector: 'app-root',
  imports: [MatIconModule, MatSidenavModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  readonly nav: NavItem[] = [
    { path: '/dashboard', label: 'Dashboard', caption: '의료 데이터 통계', icon: 'dashboard' },
    { path: '/patients', label: 'Patients', caption: '환자 목록', icon: 'group' },
    { path: '/conditions', label: 'Conditions', caption: '질병 분석', icon: 'medical_information' },
    { path: '/pipeline', label: 'Data Pipeline', caption: '처리 상태', icon: 'account_tree' },
  ];
}
