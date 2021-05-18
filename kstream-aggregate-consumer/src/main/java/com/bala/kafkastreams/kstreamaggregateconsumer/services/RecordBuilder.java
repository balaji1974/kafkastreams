package com.bala.kafkastreams.kstreamaggregateconsumer.services;

import org.springframework.stereotype.Service;

import com.bala.kafkastreams.model.DepartmentAggregate;
import com.bala.kafkastreams.model.Employee;

@Service
public class RecordBuilder {
	 public DepartmentAggregate init(){
	        DepartmentAggregate departmentAggregate = new DepartmentAggregate();
	        departmentAggregate.setEmployeeCount(0);
	        departmentAggregate.setTotalSalary(0);
	        departmentAggregate.setAvgSalary(0D);
	        return departmentAggregate;
	    }

	    public DepartmentAggregate aggregate(Employee emp, DepartmentAggregate aggValue){
	        DepartmentAggregate departmentAggregate = new DepartmentAggregate();
	        departmentAggregate.setEmployeeCount(aggValue.getEmployeeCount() + 1);
	        departmentAggregate.setTotalSalary(aggValue.getTotalSalary() + emp.getSalary());
	        departmentAggregate.setAvgSalary((aggValue.getTotalSalary() + emp.getSalary()) / (aggValue.getEmployeeCount() + 1D));
	        return departmentAggregate;
	    }
}
