Vue.component('component-paging', {
    props: ['page'],
    methods: {
        setPage: function (newPage) {
            this.page.pageNumber = newPage;
            // 触发组件的select事件
            this.$emit('select', true);
        },
        changePageSize: function () {
            this.page.pageNumber = 0;
            this.$emit('select', true);
        }
    },
    computed: {
        loopPage : function () {
            let beginPage = this.page.pageNumber - 2;
            if (beginPage < 1) {
                beginPage = 1;
            }
            let endPage = this.page.pageNumber + 4;
            if (endPage > this.page.totalPages) {
                endPage = this.page.totalPages;
            }
            let loop = new Array(0);
            for (let i = beginPage; i <= endPage; i ++) {
                loop.push(i);
            }
            return loop;
        }
    },
    template: `
        <tfoot>
            <tr v-if="page.totalElements == 0">
                <td colspan="100%" style="padding:0 !important;">
                    <div class="ibox-content ibox-heading">
                        <h3 style="padding-left: 10px;"><i class="fa fa-file-o"></i> 暂无数据</h3>
                    </div>
                </td>
            </tr>
            <tr v-else>
                <td colspan="100%">
                    <div class='row'>
                        <div class='col-sm-6'>
                            <div class='form-group form-inline' style='margin-bottom: 0 !important;'>
                                <label class='control-label'>
                                    共<span class='count_num'>{{page.totalElements}}</span>条记录, 每页条数:
                                </label>
                                <select class='form-control' @change="changePageSize" v-model="page.pageSize">
                                    <option value='10' :class="{ selected : page.pageSize == 10 }">10</option>
                                    <option value='20' :class="{ selected : page.pageSize == 20 }">20</option>
                                    <option value='50' :class="{ selected : page.pageSize == 50 }">50</option>
                                    <option value='100' :class="{ selected : page.pageSize == 100 }">100</option>
                                </select>
                            </div>
                        </div>
                        <div class='col-sm-6'>
                            <ul class='pagination pull-right' style='margin-top: 0 !important;margin-bottom: 0!important;'>
                                <!--第一页 start-->
                                <template v-if="page.pageNumber == 0">
                                    <li v-if="page.pageNumber == 0" class="footable-page-arrow disabled"><a href="javascript:;">«</a></li>
                                    <li class="footable-page-arrow disabled"><a href="javascript:;">‹</a></li>
                                </template>
                                <template v-else>
                                    <li class="footable-page-arrow"><a href="javascript:;" @click="setPage(0)">«</a></li>
                                    <li class="footable-page-arrow"><a href="javascript:;" @click="setPage(page.pageNumber - 1)">‹</a></li>
                                </template>
                                <!--第一页 end-->
                                
                                <!-- 前省略号 start -->
                                <template v-if="page.pageNumber > 3">
                                    <li class="footable-page-arrow disabled"><a>..</a></li>
                                </template>
                                <!-- 前省略号 end -->
                                
                                <template v-for="temp in loopPage">
                                    <li v-if="temp != (page.pageNumber + 1)" class="paginate_button">
                                        <a href="javascript:;" @click="setPage(temp - 1)" >{{temp}}</a>
                                    </li>
                                    <li v-else class="paginate_button active disabled">
                                        <a href="javascript:;">{{temp}}</a>
                                    </li>
                                </template>

                                <!-- 后省略号 start -->
                                <template v-if="(page.totalPages - page.pageNumber) > 2">
                                    <li class="footable-page-arrow disabled"><a>..</a></li>
                                </template>
                                <!-- 后省略号 end -->
                                
                                <!--最后一页 start-->
                                <template v-if="(page.pageNumber + 1) == page.totalPages">
                                    <li class="footable-page-arrow disabled"><a>›</a></li>
                                    <li class="footable-page-arrow disabled"><a>»</a></li>
                                </template>
                                <template v-else>
                                    <li class="footable-page-arrow"><a href="javascript:;" @click="setPage((page.pageNumber + 1))" >›</a></li>
                                    <li class="footable-page-arrow"><a href="javascript:;" @click="setPage(page.totalPages - 1)" >»</a></li>
                                </template>
                                <!--最后一页 end-->
                            </ul>
                        </div>
                    </div>
                </td>
            </tr>
        </tfoot>
    `
});