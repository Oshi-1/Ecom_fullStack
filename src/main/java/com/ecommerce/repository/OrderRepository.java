package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
	List<Order> findAllByOrderByCreatedAtDesc();

	@Query("""
			select distinct o
			from Order o
			left join fetch o.items
			where o.user.userId = :userId
			order by o.createdAt desc
			""")
	List<Order> findMyOrdersWithItems(@Param("userId") Long userId);
}
