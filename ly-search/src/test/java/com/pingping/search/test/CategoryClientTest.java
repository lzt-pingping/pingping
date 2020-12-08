package com.pingping.search.test;

import com.pingping.common.vo.PageResult;
import com.pingping.item.client.ItemClient;
import com.pingping.item.dto.SpuDTO;
import com.pingping.search.pojo.Goods;
import com.pingping.search.repository.GoodsRepository;
import com.pingping.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CategoryClientTest {

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private SearchService searchService;

    @Autowired
    private GoodsRepository goodsRepository;

    @Test
    public void loadData(){

        int page = 1;  //页码
        int size = 0;  //查询后的记录数

        do {

            PageResult<SpuDTO> spuList = itemClient.querySpuByPage(page,100, true,null  );

            List<SpuDTO> items = spuList.getItems();

            //将List<SpuDTO>中的对象通过searchService::buildGoods转成Goods对象并收集成List<Goods>集合
            List<Goods> goods = items.stream().map(searchService::buildGoods).collect(Collectors.toList());

            //统一通过Repository保存到索引库
            goodsRepository.saveAll(goods);

            size = items.size();    //将每页记录数保存到size中
            page++;  //下一页查询

            //如果当前页满足100条记录，继续查询
        }while (size == 100);

    }

    @Test
    public void testClient(){
        PageResult<SpuDTO> spuPage = itemClient.querySpuByPage(1,100, true,null  );
        List<SpuDTO> items = spuPage.getItems();
        for (SpuDTO item : items) {
            System.out.println(item);
        }
    }
}
