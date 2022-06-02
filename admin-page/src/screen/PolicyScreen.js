import * as React from 'react';
import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import UserCard from '../component/UserCard';
import ApiTestCard from '../component/ApiTestCard';
import Grid from '@mui/material/Grid';
import InterestRegionCard from '../component/InterestRegionCard';
import MyInterestRegionCard from '../component/MyInterestRegionCard';
import MyInterestCategoryCard from '../component/MyInterestCategoryCard';
import PolicyCard from '../component/PolicyCard';
import Pagination from '@mui/material/Pagination';
import Chip from '@mui/material/Chip';

import {
  Button,
  FormControl,
  Input,
  InputLabel,
  MenuItem,
  Modal,
  Paper,
  Select,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from '@mui/material';
import { axiosInstance } from '../axiosInstance';
import { Box } from '@mui/system';

export default function PolicyScreen({ user, setUser, fetch, fetchData }) {
  const [data, setData] = useState([]);
  const [open, setOpen] = useState(false);
  const handleOpen = () => setOpen(true);
  const handleClose = () => setOpen(false);
  const [reload, setReload] = useState(false);
  const reloadTrigger = () => setReload(!reload);

  const [detailOpen, setDetailOpen] = useState(false);
  const [userDetail, setUserDetail] = useState({});

  const [page, setPage] = useState(1);

  const [text, setText] = useState('');
  const [region, setRegion] = useState([]);
  const [category, setCategory] = useState([]);

  useEffect(() => {
    let reg = '';
    region.map((e) => (reg += e.region.id + ','));

    let cat = '';
    category.map((e) => (cat += e.category.id + ','));

    axiosInstance.get(`policy/search?title=${text}&region=${reg}&category=${cat}&page=${page - 1}&size=10`).then((res) => {
      setData({ ...res.data.data });
    });
  }, [page, reload]);

  const [region1, setRegion1] = useState();
  const [region2, setRegion2] = useState();
  const [regions1, setRegions1] = useState([]);
  const [regions2, setRegions2] = useState([]);

  useEffect(() => {
    axiosInstance.get(`policy/category/me`).then((res) => {
      console.log(res.data.data);
      setCategory([...res.data.data]);
      axiosInstance.get(`region/me`).then((res) => {
        console.log(res.data.data);
        setRegion([...res.data.data]);
        reloadTrigger();
      });
    });

    axiosInstance.get('region/name').then((res) => {
      console.log(res);
      setRegions1([...res.data.data]);
    });
  }, []);
  useEffect(() => {
    axiosInstance.get(`region/name?parentId=${region1}`).then((res) => {
      setRegions2([...res.data.data]);
    });
  }, [region1]);

  const [ca1, setCa1] = useState();
  const [ca2, setCa2] = useState();
  const [cas1, setCas1] = useState([]);
  const [cas2, setCas2] = useState([]);
  useEffect(() => {
    axiosInstance.get('policy/category/name').then((res) => {
      console.log(res);
      setCas1([...res.data.data]);
    });
  }, []);
  useEffect(() => {
    axiosInstance.get(`policy/category/name?parentId=${ca1}`).then((res) => {
      setCas2([...res.data.data]);
    });
  }, [ca1]);

  return (
    <div style={{ padding: 10, display: 'flex', gap: 8, flexDirection: 'column', alignItems: 'center' }}>
      {!user ? (
        <div>인증된 사용자만 확인할 수 있습니다</div>
      ) : (
        <>
          <div>
            <Input
              variant='filled'
              label='제목 검색'
              value={text}
              onChange={(e) => {
                setText(e.target.value);
              }}
            ></Input>

            <Button variant='contained' onClick={() => reloadTrigger()}>
              검색
            </Button>
          </div>
          <div style={{ width: '100%', display: 'flex', justifyContent: 'space-around' }}>
            <div>
              <FormControl>
                <InputLabel id='demo-simple-select-label'>상위카테</InputLabel>
                <Select size='small' sx={{ width: 100 }} value={ca1} onChange={(e) => setCa1(e.target.value)}>
                  {cas1.map((region, idx) => {
                    return <MenuItem value={region.id}>{region.name}</MenuItem>;
                  })}
                </Select>
              </FormControl>
              <FormControl>
                <InputLabel id='demo-simple-select-label'>하위카테</InputLabel>
                <Select
                  size='small'
                  sx={{ width: 100 }}
                  value={ca2}
                  onChange={(e) => {
                    setCategory([...category, { category: e.target.value }]);
                  }}
                >
                  {cas2.map((region, idx) => {
                    return <MenuItem value={region}>{region.name}</MenuItem>;
                  })}
                </Select>
              </FormControl>
              <div style={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                {category?.map((e, idx) => (
                  <Chip
                    label={e?.category?.parent?.name + '/' + e?.category?.name}
                    onDelete={() => setCategory([...category.splice(idx + 1, 1)])}
                  ></Chip>
                ))}
              </div>
            </div>

            <div>
              <FormControl>
                <InputLabel id='demo-simple-select-label'>지역</InputLabel>
                <Select size='small' sx={{ width: 100 }} value={region1} onChange={(e) => setRegion1(e.target.value)}>
                  {regions1.map((region, idx) => {
                    return <MenuItem value={region.id}>{region.name}</MenuItem>;
                  })}
                </Select>
              </FormControl>
              <FormControl>
                <InputLabel id='demo-simple-select-label'>상세 지역</InputLabel>
                <Select
                  size='small'
                  sx={{ width: 100 }}
                  value={region2}
                  onChange={(e) => {
                    setRegion([...region, { region: e.target.value }]);
                  }}
                >
                  {regions2.map((region, idx) => {
                    return <MenuItem value={region}>{region.name}</MenuItem>;
                  })}
                </Select>
              </FormControl>
              <div style={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                {region?.map((e, idx) => (
                  <Chip
                    label={e?.region?.parent?.name + '/' + e?.region?.name}
                    onDelete={() => {
                      let temp = [...region];
                      temp.splice(idx, 1);
                      setRegion([...temp]);
                    }}
                  ></Chip>
                ))}
              </div>
            </div>
          </div>
          <TableContainer component={Paper}>
            <Table sx={{ minWidth: '600px' }} aria-label='simple table'>
              <TableHead>
                <TableRow>
                  <TableCell align='center'>ID</TableCell>
                  <TableCell align='center'>제목</TableCell>
                  <TableCell align='center'>주관기관</TableCell>
                  <TableCell align='center'>지역</TableCell>
                  <TableCell align='center'>카테고리</TableCell>
                  <TableCell align='center'>시작일</TableCell>
                  <TableCell align='center'>종료일</TableCell>
                  <TableCell align='center'>기간</TableCell>
                  <TableCell align='center'>API 출처</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {data?.content?.map((row, idx) => (
                  <TableRow
                    key={idx}
                    sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                    onClick={(e) => {
                      e.stopPropagation();
                      setDetailOpen(true);
                      setUserDetail(row);
                    }}
                  >
                    <TableCell align='center'>{row.id}</TableCell>
                    <TableCell align='center'>{row.title}</TableCell>
                    <TableCell align='center'>{row.institution}</TableCell>
                    {/* <TableCell align='center'>{user.gender}</TableCell>
                    <TableCell align='center'>{user.birth}</TableCell>
                    <TableCell align='center'>
                      <div>{user.email.substring(0, 10)}</div>
                      <div>{user.email.substring(10)}</div>
                    </TableCell> */}
                    <TableCell align='center'>{row.region?.parent?.name + ' ' + row.region?.name}</TableCell>
                    <TableCell align='center'>{row.category?.parent?.name + ' ' + row.category?.name}</TableCell>
                    <TableCell align='center'>{row.startDate}</TableCell>
                    <TableCell align='center'>{row.endDate}</TableCell>
                    <TableCell align='center'>{row.period}</TableCell>
                    <TableCell align='center'>{row.openApiType}</TableCell>
                    {/* <TableCell align='center' width={'20px'}>
                      <Button
                        size='small'
                        variant='contained'
                        color='error'
                        onClick={(e) => {
                          e.stopPropagation();
                          if (!window.confirm('정말 삭제하시겠습니까?')) return;

                          axiosInstance
                            .delete(`user/${row.id}`)
                            .then(() => {
                              fetchData();
                            })
                            .catch((res) => {
                              alert(res.response.data.message);
                            });
                        }}
                      >
                        삭제
                      </Button>
                    </TableCell> */}
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
          <Pagination
            count={data?.totalPages}
            page={page}
            onChange={(e, value) => {
              setPage(value);
            }}
            color='primary'
          />

          <Modal open={detailOpen} onClose={() => setDetailOpen(false)}>
            <Box sx={{ ...style }}>
              {userDetail &&
                Object.entries(userDetail).map(([key, value]) => (
                  <div>
                    {key}:{' ' + value}
                  </div>
                ))}
            </Box>
          </Modal>
        </>
      )}
    </div>
  );
}

const style = {
  position: 'absolute',
  top: '50%',
  left: '50%',
  transform: 'translate(-50%, -50%)',
  bgcolor: 'background.paper',
  borderRadius: 4,
  boxShadow: 24,
  p: 4,
  maxWidth: '95vw',
};
