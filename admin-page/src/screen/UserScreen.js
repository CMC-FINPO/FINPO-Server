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
import TextField from '@mui/material/TextField';

import {
  Button,
  FormControl,
  Input,
  InputLabel,
  MenuItem,
  Modal,
  Paper,
  Select,
  Switch,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Collapse,
  Avatar,
} from '@mui/material';
import { axiosInstance } from '../axiosInstance';
import { Box } from '@mui/system';

export default function UserScreen({ user, setUser, fetch, fetchData }) {
  const [data, setData] = useState([]);
  const [open, setOpen] = useState(false);
  const handleOpen = () => setOpen(true);
  const handleClose = () => setOpen(false);
  const [reload, setReload] = useState(false);
  const reloadTrigger = () => setReload(!reload);

  const [detailOpen, setDetailOpen] = useState(false);
  const [detail, setDetail] = useState({});

  const [page, setPage] = useState(1);

  const [reports, setReports] = useState([]);
  const [userId, setUserId] = useState();
  const [reportBody, setReportBody] = useState({});

  const [banneds, setBanneds] = useState([]);

  let today = new Date();
  let utcNow = today.getTime() + today.getTimezoneOffset() * 60 * 1000;
  today = new Date(utcNow + 9 * 60 * 60 * 1000);

  const releaseDates = [
    { str: '하루', val: 1 },
    { str: '10일', val: 10 },
    { str: '한달', val: 30 },
    { str: '1년', val: 365 },
    { str: '영구정지', val: 36500 },
  ];

  useEffect(() => {
    axiosInstance.get(`user?page=${page - 1}&size=20`).then((res) => {
      setData({ ...res.data.data });
    });

    axiosInstance.get(`report/reason`).then((res) => {
      setReports([...res.data.data]);
    });

    if (userId)
      axiosInstance
        .get(`user/banned?userId=${userId}`)
        .then((res) => {
          setBanneds([...res.data.data]);
        })
        .catch((res) => {
          alert(res.response.data.message);
        });
  }, [page, reload, fetch]);

  return (
    <div style={{ padding: 10, display: 'flex', gap: 8, flexDirection: 'column', alignItems: 'center' }}>
      {!user ? (
        <div>인증된 사용자만 확인할 수 있습니다</div>
      ) : (
        <>
          <div></div>

          <TableContainer component={Paper}>
            <Table sx={{ minWidth: '600px' }} aria-label='simple table'>
              <TableHead>
                <TableRow>
                  <TableCell align='center'>ID</TableCell>
                  <TableCell align='center'>name</TableCell>
                  <TableCell align='center'>nickname</TableCell>
                  <TableCell align='center'>birth</TableCell>
                  <TableCell align='center'>gender</TableCell>
                  <TableCell align='center'>기본지역</TableCell>
                  <TableCell align='center'>프로필</TableCell>
                  <TableCell align='center'>상태</TableCell>
                  <TableCell align='center'></TableCell>
                </TableRow>
              </TableHead>
              <TableBody style={{ width: '100%' }}>
                {data?.content?.map((row, idx) => (
                  <>
                    <TableRow
                      key={idx}
                      sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                      onClick={(e) => {
                        e.stopPropagation();
                        setUserId(userId === row.id ? null : row.id);
                        axiosInstance
                          .get(`user/banned?userId=${row.id}`)
                          .then((res) => {
                            setBanneds([...res.data.data]);
                          })
                          .catch((res) => {
                            alert(res.response.data.message);
                          });
                      }}
                    >
                      <TableCell align='center'>{row.id}</TableCell>
                      <TableCell align='center'>{row.name}</TableCell>
                      <TableCell align='center'>{row.nickname}</TableCell>
                      <TableCell align='center'>{row.birth}</TableCell>
                      <TableCell align='center'>{row.gender}</TableCell>
                      <TableCell align='center'>{row.defaultRegion?.parent?.name + ' ' + row.defaultRegion?.name}</TableCell>
                      <TableCell align='center'>
                        <Avatar src={row.profileImg} />
                      </TableCell>
                      <TableCell align='center'>{{ ROLE_USER: '사용자', ROLE_ADMIN: '관리자', ROLE_BANNED_USER: '정지됨' }[row.role]}</TableCell>
                      <TableCell align='center'>
                        {row.role === 'ROLE_USER' && (
                          <Button
                            size='small'
                            variant='contained'
                            color='secondary'
                            onClick={(e) => {
                              e.stopPropagation();
                              setUserId(row.id);
                              setOpen(true);
                            }}
                          >
                            정지
                          </Button>
                        )}

                        <Button
                          size='small'
                          variant='contained'
                          color='error'
                          sx={{ marginLeft: '10px' }}
                          onClick={(e) => {
                            let access_token, code;

                            e.stopPropagation();
                            if (!window.confirm('정말 삭제하시겠습니까?')) return;

                            if (row.oAuthType === 'GOOGLE') {
                              access_token = prompt('구글 access token 입력');
                            }
                            if (row.oAuthType === 'APPLE') {
                              code = prompt('애플 code 입력');
                            }
                            axiosInstance
                              .delete(`user/${row.id}`, {
                                data: {
                                  access_token,
                                  code,
                                },
                              })
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
                      </TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell style={{ paddingBottom: 0, paddingTop: 0 }} colSpan={9}>
                        <Collapse in={row && userId && row.id === userId} timeout='auto' unmountOnExit>
                          {Boolean(banneds.length) && (
                            <>
                              <div style={{ width: '100%', padding: '20px 10px 30px 10px' }}>
                                <div style={{ fontSize: '20px', fontWeight: '600' }}>정지기록</div>

                                {banneds.map((banned, idx) => {
                                  return (
                                    <div style={{ display: 'flex', gap: 30, paddingLeft: '50px', alignItems: 'center' }}>
                                      <div>id:{banned.id}</div>
                                      <div> 사유:{banned.report.reason}</div>
                                      <div> 상세사유:{banned.detail}</div>
                                      <div> 차단일:{banned.createdAt.slice(0, 10)}</div>
                                      <div> 해제일:{banned.releaseDate}</div>
                                      {banned.releaseDate > today.toLocaleDateString('en-CA') && (
                                        <Button
                                          size='small'
                                          variant='contained'
                                          color='success'
                                          onClick={(e) => {
                                            e.stopPropagation();
                                            axiosInstance
                                              .put(`user/banned/${banned.id}?releaseNow=true`, {})
                                              .then((res) => {
                                                fetchData();
                                              })
                                              .catch((res) => {
                                                alert(res.response.data.message);
                                              });
                                          }}
                                        >
                                          정지해제
                                        </Button>
                                      )}
                                    </div>
                                  );
                                })}
                              </div>
                            </>
                          )}
                        </Collapse>
                      </TableCell>
                    </TableRow>

                    {/* {Object.entries(row).map(([key, value]) => (
                      <div>
                        {key}:{' ' + JSON.stringify(value)}
                      </div>
                    ))} */}
                  </>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
          <Pagination
            count={data?.totalPages}
            page={page}
            siblingCount={5}
            onChange={(e, value) => {
              setPage(value);
            }}
            color='primary'
          />

          <Modal open={open} onClose={handleClose} aria-labelledby='modal-modal-title' aria-describedby='modal-modal-description'>
            <Box sx={style}>
              <div style={{ width: '100%', display: 'flex', flexDirection: 'column', gap: 30 }}>
                <FormControl sx={{ width: '250px' }}>
                  <InputLabel id='demo-simple-select-label'>사유</InputLabel>
                  <Select
                    value={reportBody?.report?.id}
                    onChange={(e) => {
                      reportBody.report = { id: e.target.value };
                      setReportBody({ ...reportBody });
                    }}
                  >
                    {Boolean(reports?.length) &&
                      reports.map((report, idx) => {
                        return <MenuItem value={report.id}>{report.reason}</MenuItem>;
                      })}
                  </Select>
                </FormControl>

                <FormControl sx={{ width: '250px' }}>
                  <InputLabel id='demo-simple-select-label'>정지 기간</InputLabel>
                  <Select
                    onChange={(e) => {
                      reportBody.releaseDate = new Date(today.setDate(today.getDate() + e.target.value)).toLocaleDateString('en-CA');
                      console.log(reportBody.releaseDate);
                      setReportBody({ ...reportBody });
                    }}
                  >
                    {releaseDates.map((releseDate, idx) => {
                      return <MenuItem value={releseDate.val}>{releseDate.str}</MenuItem>;
                    })}
                  </Select>
                </FormControl>

                <TextField
                  label='상세 사유'
                  multiline
                  fullWidth
                  onChange={(e) => {
                    reportBody.detail = e.target.value;
                    setReportBody({ ...reportBody });
                  }}
                ></TextField>

                <Button
                  variant='contained'
                  onClick={(e) => {
                    e.stopPropagation();
                    handleClose();
                    reportBody.user = { id: userId };
                    console.log(reportBody);
                    axiosInstance
                      .post(`user/banned`, reportBody)
                      .then((res) => {
                        fetchData();
                      })
                      .catch((res) => {
                        alert(res.response.data.message);
                      });
                  }}
                >
                  차단
                </Button>
              </div>
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
