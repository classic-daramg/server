package com.daramg.server.notice.repository;

import com.daramg.server.notice.domain.Notice;
import com.daramg.server.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Notice n
        set n.user = :admin
        where n.user.id = :userId
                  and n.isDeleted = false
        """)
    int transferToAdmin(@Param("userId") Long userId, @Param("admin") User admin);
}
