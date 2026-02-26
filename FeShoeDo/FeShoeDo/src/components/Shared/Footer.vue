<template>
  <div class="footer">
    <div class="container">
      <div class="footer-bottom">
        <p>© 2025 Shop ShoeDo. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Footer'
}
</script>

<style scoped>
.footer {
  background: #ffffff;
  margin-top: 40px;
  padding: 40px 0 20px;
  border-top: 4px solid #000000;
  border-radius: 8px 8px 0 0;
}

.footer h5 {
  font-size: 14px;
  color: #000000;
  margin-bottom: 15px;
  text-transform: uppercase;
}

.footer ul {
  list-style: none;
  padding: 0;
}

.footer li {
  margin-bottom: 10px;
}

.footer a {
  color: #000000;
  text-decoration: none;
  font-size: 13px;
}

.footer a:hover {
  color: #333333;
}

.footer-bottom {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #000000;
  color: #000000;
  font-size: 12px;
}
</style>