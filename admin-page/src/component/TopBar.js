import * as React from 'react';
import { useEffect, useState } from 'react';

import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import Toolbar from '@mui/material/Toolbar';
import IconButton from '@mui/material/IconButton';
import Typography from '@mui/material/Typography';
import Badge from '@mui/material/Badge';
import MenuItem from '@mui/material/MenuItem';
import Menu from '@mui/material/Menu';
import AccountCircle from '@mui/icons-material/AccountCircle';
import NotificationsIcon from '@mui/icons-material/Notifications';
import MoreIcon from '@mui/icons-material/MoreVert';
import SideBar from './SideBar';
import Button from '@mui/material/Button';
import { RiKakaoTalkFill } from 'react-icons/ri';
import { styled } from '@mui/material';
import { grey, indigo, yellow } from '@mui/material/colors';
import GoogleIcon from '@mui/icons-material/Google';
import AppleIcon from '@mui/icons-material/Apple';
import { axiosInstance } from '../axiosInstance';
import jwt_decode from 'jwt-decode';

export default function TopBar({ user, setUser, fetch, fetchData }) {
  const [anchorEl, setAnchorEl] = React.useState(null);
  const [mobileMoreAnchorEl, setMobileMoreAnchorEl] = React.useState(null);

  const isMenuOpen = Boolean(anchorEl);
  const isMobileMenuOpen = Boolean(mobileMoreAnchorEl);

  useEffect(() => {
    if (localStorage.getItem('accessToken'))
      axiosInstance
        .post('oauth/reissue', {
          accessToken: localStorage.getItem('accessToken'),
          refreshToken: localStorage.getItem('refreshToken'),
        })
        .then((res) => {
          localStorage.setItem('accessToken', res.data.data.accessToken);
          localStorage.setItem('refreshToken', res.data.data.refreshToken);
          setUser(jwt_decode(res.data.data.accessToken));
        });

    axiosInstance.get('user/me').then((e) => {
      if (e.data.data === null) setUser(null);
    });
  }, [fetch]);

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
      <AppleButton sx={{ fontSize: 16 }} variant='contained' size='small' startIcon={<AppleIcon />} onClick={() => {}}>
        {'준비 중'}
      </AppleButton>
    );
  };

  const GoogleLogin = () => {
    return (
      <GoogleButton sx={{ fontSize: 16 }} variant='contained' size='small' startIcon={<GoogleIcon />} onClick={() => {}}>
        {'준비 중'}
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

  return (
    <Box sx={{ flexGrow: 1 }}>
      <AppBar position='static'>
        <Toolbar>
          <SideBar />
          <Typography variant='h6' noWrap component='div' sx={{ display: { xs: 'none', sm: 'block' } }}>
            FINPO Admin page
          </Typography>

          <Box sx={{ flexGrow: 1 }} />
          <Box sx={{ display: { xs: 'none', md: 'flex', gap: 10 } }}>
            <KakaoLogin />

            <AppleLogin />

            <GoogleLogin />
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
