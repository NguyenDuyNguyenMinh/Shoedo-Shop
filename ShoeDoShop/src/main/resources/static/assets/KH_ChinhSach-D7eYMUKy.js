import{A as e,C as t,F as n,L as r,M as i,O as a,S as o,_ as s,a as c,b as l,k as u,m as d,z as f}from"./index-Dqpz_6bO.js";import"./api-Ca89L4FN.js";import{n as p,t as m}from"./Footer-BYG4-jsF.js";import{t as h}from"./ChatBox-pgwsZE-d.js";var g={name:`KH_ChinhSach`,components:{KH_Navbar:p,Footer:m,ChatBox:h},setup(){return{policies:n([{icon:`bi bi-credit-card`,title:`1. Chính sách thanh toán`,content:`
          <p class="mb-2"><strong>Hình thức thanh toán:</strong></p>
          <ul class="policy-list">
            <li><i class="bi bi-check-circle-fill text-success me-2"></i>Thanh toán khi nhận hàng (COD)</li>
            <li><i class="bi bi-check-circle-fill text-success me-2"></i>Thanh toán qua
              
              <img src="/vnpay_thumb.png" alt="VNPay" style="height: 26px; width: auto; display: inline-block;">
            </li>
          </ul>
        `},{icon:`bi bi-truck`,title:`2. Chính sách giao hàng`,content:`
          <ul class="policy-list">
            <li><i class="bi bi-clock-history me-2"></i><strong>Thời gian:</strong> 3 – 7 ngày làm việc (tùy khu vực)</li>
            <li><i class="bi bi-calendar-x me-2"></i><strong>Không tính:</strong> Ngày lễ, Tết hoặc trường hợp bất khả kháng (thời tiết, dịch bệnh, đơn vị vận chuyển quá tải)</li>
            <li><i class="bi bi-box-seam me-2"></i><strong>Kiểm tra hàng:</strong> Khách được kiểm tra ngoại quan trước khi thanh toán (không mang thử khi chưa thanh toán nếu đơn vị vận chuyển không cho phép)</li>
          </ul>
        `},{icon:`bi bi-arrow-repeat`,title:`3. Chính sách đổi hàng`,content:`
          <div class="mb-3">
            <p class="fw-bold mb-2">3.1 Điều kiện đổi:</p>
            <ul class="policy-list">
              <li><i class="bi bi-check-circle text-success me-2"></i>Sản phẩm lỗi do nhà sản xuất (bung keo, lệch kiểu dáng, rách hỏng)</li>
              <li><i class="bi bi-check-circle text-success me-2"></i>Giao sai mẫu, sai size</li>
              <li><i class="bi bi-check-circle text-success me-2"></i>Thời hạn: 30 ngày kể từ khi nhận hàng</li>
              <li><i class="bi bi-check-circle text-success me-2"></i>Sản phẩm chưa sử dụng, còn nguyên tem, phụ kiện đi kèm</li>
              <li><i class="bi bi-check-circle text-success me-2"></i>Có video mở hàng để chứng minh đối chiếu</li>
            </ul>
          </div>
          <div class="mb-3">
            <p class="fw-bold mb-2 text-danger">3.2 Không hỗ trợ đổi:</p>
            <ul class="policy-list">
              <li><i class="bi bi-x-circle text-danger me-2"></i>Đã qua sử dụng, dơ bẩn, trầy xước do người dùng</li>
              <li><i class="bi bi-x-circle text-danger me-2"></i>Hư hỏng do bảo quản sai cách hoặc do tác động bên ngoài</li>
              <li><i class="bi bi-x-circle text-danger me-2"></i>Sản phẩm xả kho, giảm giá sâu (trừ trường hợp có lỗi từ nhà sản xuất)</li>
            </ul>
          </div>
          <div>
            <p class="fw-bold mb-2">3.3 Phí đổi:</p>
            <ul class="policy-list">
              <li><i class="bi bi-shield-check me-2"></i>Lỗi từ shop: Miễn phí vận chuyển</li>
              <li><i class="bi bi-cash me-2"></i>Đổi do nhu cầu cá nhân: Khách chịu phí vận chuyển 2 chiều</li>
            </ul>
          </div>
        `},{icon:`bi bi-shield-check`,title:`4. Chính sách bảo hành`,content:`
          <ul class="policy-list">
            <li><i class="bi bi-clock me-2"></i><strong>Thời gian:</strong> 1 tháng (tùy dòng sản phẩm)</li>
            <li><i class="bi bi-check-circle me-2"></i><strong>Bảo hành:</strong> Lỗi kỹ thuật từ nhà sản xuất (bung keo, xứt chỉ, hở đế)</li>
          </ul>
          <p class="fw-bold mt-3 mb-2">Không áp dụng bảo hành cho:</p>
          <ul class="policy-list">
            <li><i class="bi bi-dash-circle text-danger me-2"></i>Hao mòn tự nhiên trong quá trình sử dụng</li>
            <li><i class="bi bi-dash-circle text-danger me-2"></i>Hư hỏng do ngâm nước, giặt máy, phơi nắng gắt</li>
            <li><i class="bi bi-dash-circle text-danger me-2"></i>Trầy xước, mòn đế do ma sát khi sử dụng</li>
          </ul>
        `},{icon:`bi bi-shield-lock`,title:`5. Chính sách bảo mật`,content:`
          <ul class="policy-list">
            <li><i class="bi bi-lock-fill me-2"></i>Cam kết bảo mật tuyệt đối thông tin khách hàng</li>
            <li><i class="bi bi-file-text me-2"></i>Chỉ sử dụng để xử lý đơn hàng và chăm sóc khách</li>
            <li><i class="bi bi-shield-slash me-2"></i>Không chia sẻ cho bên thứ ba khi chưa đồng ý</li>
          </ul>
        `},{icon:`bi bi-cash-coin`,title:`6. Chính sách hoàn tiền`,content:`
          <ul class="policy-list">
            <li><i class="bi bi-check-circle me-2"></i>Áp dụng khi shop hết hàng thay thế</li>
            <li><i class="bi bi-clock-history me-2"></i>Thời gian xử lý: 3 – 7 ngày làm việc</li>
            <li><i class="bi bi-bank me-2"></i>Hình thức: Chuyển khoản ngân hàng</li>
          </ul>
        `},{icon:`bi bi-file-text`,title:`7. Điều khoản chung`,content:`
          <ul class="policy-list">
            <li><i class="bi bi-pencil-square me-2"></i>Shop có quyền thay đổi chính sách không cần báo trước</li>
            <li><i class="bi bi-telephone me-2"></i>Mọi thắc mắc vui lòng liên hệ qua kênh chính thức</li>
          </ul>
        `}])}}},_={class:`customer-layout`},v={class:`container py-4`},y={class:`row g-4`},b={class:`policy-card h-100`},x={class:`policy-header`},S={class:`policy-icon`},C={class:`policy-title`},w=[`innerHTML`],T={class:`text-center mt-5 pt-4`},E={class:`brand-message d-flex align-items-center justify-content-center gap-3 flex-wrap`};function D(n,c,p,m,h,g){let D=e(`KH_Navbar`),O=e(`router-link`),k=e(`Footer`),A=e(`ChatBox`);return a(),l(`div`,_,[t(D),s(`main`,v,[c[2]||=s(`div`,{class:`text-center mb-5`},[s(`h1`,{class:`display-5 fw-bold text-uppercase position-relative d-inline-block`},[o(` Chính Sách `),s(`span`,{class:`header-underline`})]),s(`p`,{class:`text-muted mt-3`},`Cam kết mang đến trải nghiệm mua sắm tốt nhất cho khách hàng`)],-1),s(`div`,y,[(a(!0),l(d,null,u(m.policies,(e,t)=>(a(),l(`div`,{key:t},[s(`div`,b,[s(`div`,x,[s(`div`,S,[s(`i`,{class:r(e.icon)},null,2)]),s(`h3`,C,f(e.title),1)]),s(`div`,{class:`policy-content`,innerHTML:e.content},null,8,w)])]))),128))]),s(`div`,T,[s(`div`,E,[t(O,{to:`/customer/index`,class:`d-inline-block`},{default:i(()=>[...c[0]||=[s(`img`,{src:`/SHOEDO.png`,alt:`Shoedo`,class:`brand-logo`,style:{"max-width":`40px`,height:`auto`}},null,-1)]]),_:1}),c[1]||=s(`div`,{class:`text-start`},[s(`p`,{class:`lead fw-bold mb-0`},`SHOEDO - Localbrand giày Việt`),s(`p`,{class:`text-muted small mb-0`},`Cám ơn bạn đã tin tưởng và ủng hộ shop!`)],-1)])])]),t(k),t(A)])}var O=c(g,[[`render`,D],[`__scopeId`,`data-v-fdb38ed8`]]);export{O as default};