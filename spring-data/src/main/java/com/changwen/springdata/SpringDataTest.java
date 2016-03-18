package com.changwen.springdata;

import com.changwen.springdata.dao.PersonRepository;
import com.changwen.springdata.pojo.Person;
import com.changwen.springdata.service.PersonService;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * SpringDataTest
 *
 * @author lcw 2015/12/20
 */
public class SpringDataTest {
    private ApplicationContext ctx = null;
    private PersonRepository personRepository = null;
    private PersonService personService;


    {
        ctx = new ClassPathXmlApplicationContext("spring-config.xml");
        personRepository = ctx.getBean(PersonRepository.class);
        personService = ctx.getBean(PersonService.class);

    }
    /**
     * 目标: 实现带查询条件的分页. id > 5 的条件
     *
     * 调用 JpaSpecificationExecutor 的 Page<T> findAll(Specification<T> spec, Pageable pageable);
     * Specification: 封装了 JPA Criteria 查询的查询条件
     * Pageable: 封装了请求分页的信息: 例如 pageNo, pageSize, Sort
     */
    @Test
    public void testJpaSpecificationExecutor(){
        int pageNo = 3 - 1;
        int pageSize = 5;
        PageRequest pageable = new PageRequest(pageNo, pageSize);

        //通常使用 Specification 的匿名内部类
        Specification<Person> specification = new Specification<Person>() {
            /**
             * 参数@param *root: 代表查询的实体类.
             * @param query: 可以从中可到 Root 对象, 即告知 JPA Criteria 查询要查询哪一个实体类. 还可以
             * 来添加查询条件, 还可以结合 EntityManager 对象得到最终查询的 TypedQuery 对象.
             * 参数@param *cb: CriteriaBuilder 对象. 用于创建 Criteria 相关对象的工厂. 当然可以从中获取到 Predicate 对象
             * 参数@return: *Predicate 类型, 代表一个查询条件.
             */
            //@Override
            public Predicate toPredicate(Root<Person> root,
                                         CriteriaQuery<?> query, CriteriaBuilder cb) {
                Path path = root.get("id");
                Predicate predicate = cb.gt(path, 5);
                return predicate;
            }
        };

        Page<Person> page = personRepository.findAll(specification, pageable);

        System.out.println("总记录数: " + page.getTotalElements());
        System.out.println("当前第几页: " + (page.getNumber() + 1));
        System.out.println("总页数: " + page.getTotalPages());
        System.out.println("当前页面的 List: " + page.getContent());
        System.out.println("当前页面的记录数: " + page.getNumberOfElements());
    }

    @Test
    public void testPagingAndSortingRespository(){
        //pageNo 从 0 开始.
        int pageNo = 3 - 1;
        int pageSize = 5;
        //Pageable 接口通常使用的其 PageRequest 实现类. 其中封装了需要分页的信息
        //排序相关的. Sort 封装了排序的信息
        //Order 是具体针对于某一个属性进行升序还是降序.
        Sort.Order order1 = new Sort.Order(Sort.Direction.DESC, "id");
        Sort.Order order2 = new Sort.Order(Sort.Direction.ASC, "email");
        Sort sort = new Sort(order1, order2);

        PageRequest pageable = new PageRequest(pageNo, pageSize, sort);
        Page<Person> page = personRepository.findAll(pageable);

        System.out.println("总记录数: " + page.getTotalElements());
        System.out.println("当前第几页: " + (page.getNumber() + 1));
        System.out.println("总页数: " + page.getTotalPages());
        System.out.println("当前页面的 List: " + page.getContent());
        System.out.println("当前页面的记录数: " + page.getNumberOfElements());
    }
    /**
     * 批量保存
     */
    @Test
    public void testCrudReposiory(){
        List<Person> persons = new ArrayList<Person>();

        for(int i = 'a'; i <= 'z'; i++){
            Person person = new Person();
            person.setAddressId(i + 1);
            person.setBirth(new Date());
            person.setEmail((char)i + "" + (char)i + "@qq.com");
            person.setLastName((char)i + "" + (char)i);

            persons.add(person);
        }

        personService.savePersons(persons);
    }
    /****************************************/
    @Test
    public void testModifying(){
        //personService.updatePersonEmail("2", "mmmm@atguigu.com");
        personService.updatePersonEmail("mmmm@atguigu.com", 2);
    }

    @Test
    public void testNativeQuery(){
        long count = personRepository.getTotalCount();
        System.out.println(count);
    }

    @Test
    public void testQueryAnnotationLikeParam(){
		List<Person> persons = personRepository.testQueryAnnotationLikeParam("AA", "1");
		System.out.println(persons.size());

//		List<Person> persons = personRepository.testQueryAnnotationLikeParam("A", "bb");
//		System.out.println(persons.size());

//        List<Person> persons = personRepository.testQueryAnnotationLikeParam2("bb", "A");
//        System.out.println(persons.size());
    }

    @Test
    public void testQueryAnnotationParams2(){
        List<Person> persons = personRepository.testQueryAnnotationParams2("1", "AA");
        System.out.println(persons);
    }

    @Test
    public void testQueryAnnotationParams1(){
        List<Person> persons = personRepository.testQueryAnnotationParams1("AA", "1");
        System.out.println(persons);
    }
    /**
     * 查询 id 值最大的那个 Person
     */
    @Test
    public void testQueryAnnotation(){
        Person person = personRepository.getMaxIdPerson();
        System.out.println(person);
    }

    @Test
    public void testKeyWords() {
        List<Person> persons = personRepository.getByLastNameStartingWithAndIdLessThan("B", 10);
        System.out.println(persons);

        persons = personRepository.getByLastNameEndingWithAndIdLessThan("C", 10);
        System.out.println(persons);

        persons = personRepository.getByEmailInAndBirthLessThan(Arrays.asList("AA@atguigu.com", "FF@atguigu.com",
                "SS@atguigu.com"), new Date());
        System.out.println(persons.size());
    }


    @Test
    public void test() {
        Person person = personRepository.getByLastName("AA");
        System.out.println(person);
    }
    @Test
    public void testJpa() {

    }
    @Test
    public void testDataSource() {
        DataSource dataSource = ctx.getBean(DataSource.class);
        System.out.println(dataSource);
    }
}
