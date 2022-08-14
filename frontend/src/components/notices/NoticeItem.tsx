import { Link } from "react-router-dom";
import { Notice } from "../../store/notice";

import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import styles from "./NoticeItem.module.scss";
const NoticeItem: React.FC<{ notice: Notice }> = ({ notice }) => {
  const options = {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
  };
  // console.log(notice)/
  return (
      <Link to={`/notice/detail/${notice.noticeId}`} className={styles.container} >

            <div>{notice.title}</div>
            <div className={styles.noticeDate}>
              <div>{new Date(notice!.modifiedDate!).toLocaleDateString("ko-Kr", {
                year: "numeric",
                month: "long",
                day: "numeric",
              })}{" "}
              | 관리자</div><div><ChevronRightIcon /></div>
            </div>
      </Link>
  );
};

export default NoticeItem;
