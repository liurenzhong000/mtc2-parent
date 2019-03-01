package io.mtc.facade.backend.controller;

import lombok.extern.slf4j.Slf4j;

/**
 * 广告：加载页或banner
 *
 * @author Chinhin
 * 2018/8/27
 */
@Slf4j
//@RestController
//@Transactional
//@RequestMapping("/adv")
@Deprecated
public class AdvController {

//    @Resource
//    private AdvRepostitory advRepostitory;
//
//    @PreAuthorize("hasAuthority('adv:insert')")
//    @PutMapping
//    public String insert(String url, String file, Integer type) {
//        Adv adv = new Adv();
//        adv.setFile(file);
//        adv.setUrl(url);
//        adv.setType(type);
//        advRepostitory.save(adv);
//        return ResultUtil.success(adv);
//    }
//
//    @PreAuthorize("hasAuthority('adv:delete')")
//    @DeleteMapping("/{id}")
//    public String delete(@PathVariable Long id) {
//        advRepostitory.deleteById(id);
//        return ResultUtil.success("删除成功");
//    }
//
//    @PreAuthorize("hasAuthority('adv:update')")
//    @PostMapping
//    public String update(Long id, String url, String file, Integer type) {
//        Adv adv = advRepostitory.findById(id).get();
//        adv.setUrl(url);
//        adv.setFile(file);
//        adv.setType(type);
//        advRepostitory.save(adv);
//        return ResultUtil.success(adv);
//    }
//
//    @PreAuthorize("hasAuthority('adv:select')")
//    @Transactional(readOnly = true)
//    @GetMapping
//    public String select(Integer type, @ModelAttribute PagingModel pageModel) {
//        Specification<Adv> specification = (Specification<Adv>) (root, criteriaQuery, criteriaBuilder) -> {
//            List<Predicate> list = new ArrayList<>();
//            if (type != null) {
//                list.add(criteriaBuilder.equal(root.get("type"), type));
//            }
//            return CriteriaUtil.result(list, criteriaBuilder);
//        };
//        return PagingResultUtil.list(advRepostitory.findAll(specification, pageModel.make()));
//    }
//
//    /**
//     * 改变广告状态
//     * @param id 币种id
//     * @param type 1:有效，2:launchScreen显示
//     * @return 更新后的币种json字符串
//     */
//    @PostMapping("/changeStat/{id}")
//    public String changeStat(@PathVariable Long id, int type) {
//        Adv adv = advRepostitory.findById(id).get();
//        if (type == 1) {
//            adv.setIsActive(!adv.getIsActive());
//        } else if (type == 2) {
//            advRepostitory.setAllNotLaunchScreen();
//            adv.setIsLaunchScreen(!adv.getIsLaunchScreen());
//        }
//        advRepostitory.save(adv);
//        return ResultUtil.success(adv);
//    }

}
