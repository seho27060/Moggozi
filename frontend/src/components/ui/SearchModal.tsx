import React, { ReactElement } from "react";
import styles from "./SearchModal.module.scss";

interface Props {
  open: boolean;
  close: () => void;
  children: React.ReactNode;
}

const SearchModal = (props: Props): ReactElement => {
  // 열기, 닫기, 모달 헤더 텍스트를 부모로부터 받아옴
  const { open, close } = props;

  
  return (
    // 모달이 열릴때 openModal 클래스가 생성된다.

    <div onClick={ close }
      className={
        open ? `${styles.openModal} ${styles.modal}` : `${styles.modal}`
      }
    >
      {/* <div className={open ? 'openModal modal' : 'modal'}> */}
      {open ? <section onClick={(event) => { event.stopPropagation(); }}>{props.children}
      </section> : null}
      
    </div>
  );
};

export default SearchModal;
