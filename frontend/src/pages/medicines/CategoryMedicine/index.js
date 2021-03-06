import { useDispatch, useSelector } from "react-redux";
import { useHistory, useLocation } from "react-router";
import CategoryMedicineTemplate from "../../../components/templates/CategoryMedicineTemplate";
import { doNotRefresh, getCategoryMedicines } from "../../../modules/medicine";

function CategoryMedicine() {
  const dispatch = useDispatch();
  const history = useHistory();
  const categoryMedicines = useSelector(
    (state) => state.medicineReducer.categoryMedicines
  );
  const location = useLocation();
  const categoryName = location.state.categoryName;

  function onMedicineClick(medicineId) {
    dispatch(doNotRefresh());
    history.push(`/medicines/${medicineId}`);
  }

  async function handleInfiniteScroll(e) {
    const { scrollTop, clientHeight, scrollHeight } = e.target;
    if (parseInt(scrollTop) + parseInt(clientHeight) !== parseInt(scrollHeight))
      return;
    if (categoryMedicines && categoryMedicines.length % 10 !== 0) return;
    const page = parseInt(categoryMedicines.length / 10);
    await dispatch(getCategoryMedicines(categoryName, page));
  }

  return (
    <CategoryMedicineTemplate
      onMedicineClick={onMedicineClick}
      handleInfiniteScroll={handleInfiniteScroll}
    />
  );
}

export default CategoryMedicine;
