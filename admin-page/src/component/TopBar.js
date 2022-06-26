import * as React from 'react';
import { useEffect, useState } from 'react';

import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import Toolbar from '@mui/material/Toolbar';
import IconButton from '@mui/material/IconButton';
import Typography from '@mui/material/Typography';
import Badge from '@mui/material/Badge';
import MenuItem from '@mui/material/MenuItem';
import HomeIcon from '@mui/icons-material/Home';

import AppleSignin from 'react-apple-signin-auth';
import { appleAuthHelpers } from 'react-apple-signin-auth';
import Menu from '@mui/material/Menu';
import AccountCircle from '@mui/icons-material/AccountCircle';
import NotificationsIcon from '@mui/icons-material/Notifications';
import MoreIcon from '@mui/icons-material/MoreVert';
import SideBar from './SideBar';
import Button from '@mui/material/Button';
import { RiKakaoTalkFill } from 'react-icons/ri';
import { styled } from '@mui/material';
import { green, grey, indigo, yellow } from '@mui/material/colors';
import GoogleIcon from '@mui/icons-material/Google';
import AppleIcon from '@mui/icons-material/Apple';
import { axiosInstance } from '../axiosInstance';
import jwt_decode from 'jwt-decode';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

export default function TopBar({ user, setUser, fetch, fetchData }) {
  const [anchorEl, setAnchorEl] = React.useState(null);
  const [mobileMoreAnchorEl, setMobileMoreAnchorEl] = React.useState(null);

  const isMenuOpen = Boolean(anchorEl);
  const isMobileMenuOpen = Boolean(mobileMoreAnchorEl);

  const handleProfileMenuOpen = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMobileMenuClose = () => {
    setMobileMoreAnchorEl(null);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    handleMobileMenuClose();
  };

  const handleMobileMenuOpen = (event) => {
    setMobileMoreAnchorEl(event.currentTarget);
  };

  const menuId = 'primary-search-account-menu';
  const renderMenu = (
    <Menu
      anchorEl={anchorEl}
      anchorOrigin={{
        vertical: 'top',
        horizontal: 'right',
      }}
      id={menuId}
      keepMounted
      transformOrigin={{
        vertical: 'top',
        horizontal: 'right',
      }}
      open={isMenuOpen}
      onClose={handleMenuClose}
    >
      <MenuItem onClick={handleMenuClose}>Profile</MenuItem>
      <MenuItem onClick={handleMenuClose}>My account</MenuItem>
    </Menu>
  );

  const mobileMenuId = 'primary-search-account-menu-mobile';

  const KakakoButton = styled(Button)(({ theme }) => ({
    color: theme.palette.getContrastText(yellow[500]),
    backgroundColor: yellow[500],
    '&:hover': {
      backgroundColor: yellow[700],
    },
  }));

  const AppleButton = styled(Button)(({ theme }) => ({
    color: theme.palette.getContrastText(grey[300]),
    backgroundColor: grey[300],
    '&:hover': {
      backgroundColor: grey[700],
    },
  }));

  const GoogleButton = styled(Button)(({ theme }) => ({
    color: theme.palette.getContrastText(indigo[500]),
    backgroundColor: indigo[500],
    '&:hover': {
      backgroundColor: indigo[700],
    },
  }));

  const ReloadButton = styled(Button)(({ theme }) => ({
    color: theme.palette.getContrastText(green[500]),
    backgroundColor: green[500],
    '&:hover': {
      backgroundColor: green[700],
    },
  }));
  const MyAppleSigninButton = () => (
    <AppleSignin
      /** Auth options passed to AppleID.auth.init() */
      authOptions={{
        /** Client ID - eg: 'com.example.com' */
        clientId: process.env.REACT_APP_APPLE_CLIENT_ID,
        /** Requested scopes, seperated by spaces - eg: 'email name' */
        scope: 'email',
        /** Apple's redirectURI - must be one of the URIs you added to the serviceID - the undocumented trick in apple docs is that you should call auth from a page that is listed as a redirectURI, localhost fails */
        // redirectURI: 'https://example.com',
        /** State string that is returned with the apple response */
        state: 'state',
        redirectURI: process.env.REACT_APP_SERVER_URL + '/oauth/login/apple',
        /** Nonce */
        nonce: 'nonce',
        /** Uses popup auth instead of redirection */
        usePopup: false,
      }} // REQUIRED
      /** General props */
      uiType='dark'
      /** className */
      className='apple-auth-btn'
      /** Removes default style tag */
      noDefaultStyle={false}
      /** Allows to change the button's children, eg: for changing the button text */
      buttonExtraChildren='Continue with Apple'
      /** Extra controlling props */
      /** Called upon signin success in case authOptions.usePopup = true -- which means auth is handled client side */
      onSuccess={(response) => {
        console.log(response);
      }} // default = undefined
      /** Called upon signin error */
      onError={(error) => console.error('에러', error)} // default = undefined
      /** Skips loading the apple script if true */
      skipScript={false} // default = undefined
      /** Apple image props */
      iconProp={{ style: { marginTop: '10px' } }} // default = undefined
      /** render function - called with all props - can be used to fully customize the UI by rendering your own component  */
      render={(props) => <button {...props}>애플로그인</button>}
    />
  );

  const logout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    setUser(null);
    fetchData();
  };

  const KakaoLogin = () => {
    return (
      <KakakoButton
        sx={{ fontSize: 16 }}
        variant='contained'
        size='small'
        startIcon={<RiKakaoTalkFill />}
        onClick={() => {
          user ? logout() : (window.location.href = process.env.REACT_APP_KAKAO_LOGIN_URL);
        }}
      >
        {user ? `${user.nickname} (로그아웃)` : '카카오로 로그인'}
      </KakakoButton>
    );
  };

  const AppleLogin = () => {
    return (
      // <AppleButton sx={{ fontSize: 16 }} variant='contained' size='small' startIcon={<AppleIcon />} onClick={() => {}}>
      //   {'준비 중'}
      // </AppleButton>
      <>{user ? <button onClick={() => logout()}>`${user.nickname} (로그아웃)`</button> : <MyAppleSigninButton />}</>
    );
  };

  const GoogleLogin = () => {
    return (
      <GoogleButton
        sx={{ fontSize: 16 }}
        variant='contained'
        size='small'
        startIcon={<GoogleIcon />}
        onClick={() => {
          user ? logout() : (window.location.href = axiosInstance.defaults.baseURL + '/oauth/googleloginurl');
        }}
      >
        {user ? `${user.nickname} (로그아웃)` : '구글로 로그인'}
      </GoogleButton>
    );
  };

  const renderMobileMenu = (
    <Menu
      anchorEl={mobileMoreAnchorEl}
      anchorOrigin={{
        vertical: 'top',
        horizontal: 'right',
      }}
      id={mobileMenuId}
      keepMounted
      transformOrigin={{
        vertical: 'top',
        horizontal: 'right',
      }}
      open={isMobileMenuOpen}
      onClose={handleMobileMenuClose}
    >
      <MenuItem>
        <KakaoLogin />
      </MenuItem>
      <MenuItem>
        <AppleLogin />
      </MenuItem>
      <MenuItem>
        <GoogleLogin />
      </MenuItem>
    </Menu>
  );

  const navigate = useNavigate();

  return (
    <Box sx={{ flexGrow: 1 }}>
      <AppBar position='static'>
        <Toolbar>
          <SideBar />
          <IconButton size='large' edge='start' color='inherit' sx={{ mr: 2 }} onClick={() => navigate('/')}>
            <HomeIcon />
          </IconButton>
          <Typography variant='h6' noWrap component='div' sx={{ display: { xs: 'none', sm: 'block' } }}>
            FINPO Admin page
          </Typography>

          <Box sx={{ flexGrow: 1 }} />
          <Box sx={{ display: { xs: 'none', md: 'flex', gap: 10 } }}>
            <ReloadButton onClick={() => window.location.reload()}>새로고침</ReloadButton>

            {(!user || user?.oAuthType === 'KAKAO') && <KakaoLogin />}
            {(!user || user?.oAuthType === 'GOOGLE') && <GoogleLogin />}
            {(!user || user?.oAuthType === 'APPLE') && <AppleLogin />}
          </Box>
          <Box sx={{ display: { xs: 'flex', md: 'none' } }}>
            <IconButton size='large' onClick={handleMobileMenuOpen} color='inherit'>
              <MoreIcon />
            </IconButton>
          </Box>
        </Toolbar>
        {renderMobileMenu}
        {renderMenu}
      </AppBar>
    </Box>
  );
}
