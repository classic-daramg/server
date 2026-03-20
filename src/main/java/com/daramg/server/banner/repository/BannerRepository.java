package com.daramg.server.banner.repository;

import com.daramg.server.banner.domain.Banner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {

    List<Banner> findAllByOrderByOrderIndexAsc();
}
