import { Routes } from '@angular/router';

export const routes: Routes = [
    {
        path: '', 
        loadComponent: () => import('./home/home').then(m => m.HomeComponent)
    },
    {
        path:'evaluador',
        loadComponent: () => import('./evaluador/evaluador').then(m => m.EvaluadorComponent)
    },
    {
        path:'login',
        loadComponent: () => import('./login/login').then(m => m.LoginComponent)
    },
    {
        path: 'register',
        loadComponent: () => import('./register/register').then(m => m.RegisterComponent)
    },
    {
        path: 'history',
        loadComponent: () => import('./history/history').then(m => m.HistoryComponent)
    },
    {
        path: 'about',
        loadComponent: () => import('./about/about').then(m => m.AboutComponent)
    },
    {
        path:'**',
        redirectTo: ''
    }
];
