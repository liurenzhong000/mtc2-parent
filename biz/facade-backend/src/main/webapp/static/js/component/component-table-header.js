Vue.component('component-table-header', {
    props: ['sorting', 'header'],
    methods: {
        doSort : function(property) {
            // 点的另外的属性
            if (this.sorting.sort !== property) {
                this.sorting.order = '';
            }
            this.sorting.sort = property;
            if (this.sorting.order) {
                this.sorting.order = (this.sorting.order === "ASC") ? "DESC" : "ASC";
            } else {
                this.sorting.order = "DESC";
            }
            // 触发组件的select事件
            this.$emit('select', true);
        }
    },
    template: `
    <tr>
        <th v-for="temp in header"
           :class="{sortable : temp.sortable, sorted : (sorting.sort === temp.property), asc : (sorting.order === 'ASC' && sorting.sort === temp.property), desc : (sorting.order === 'DESC' && sorting.sort === temp.property)}"
           data-sort-ignore="true">
            <template v-if="temp.sortable">
                <a href="javascript:void(0);" @click="doSort(temp.property)">{{temp.name}}</a>
            </template>
            <template v-else>
                <th data-sort-ignore="true">{{temp.name}}</th>
            </template>
        </th>
    </tr>
    `
});