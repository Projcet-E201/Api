package com.example.data.entity.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class SensorInfo {

	/* 센서 고유번호 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/* 센서 제품코드 */
	@Column
	private String sensorId;

	/* 센서 타입 */
	@Column
	private String type;

	/* 마지막 교체일 */
	@Column
	private String lastDate;

	/* 동작 여부 */
	@Column
	private Boolean working;

}
